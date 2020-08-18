/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mec.auth.server.config.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.mec.auth.server.config.OAuthClientDetail;
import org.mec.auth.server.config.OAuthClientDetailsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.StringUtils;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {
    @Value("${jwt.publicKey}")
    private String publicKeyStr;

    @Value("${jwt.encryptedPrivateKey}")
    private String encryptedPrivateKey;

    @Value("${jwt.encryptPassword}")
    private String encryptPasswordStr;

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

    @Autowired
    private MecUserDetailsService mecUserDetailsService;

    @Autowired
    private AuthInMemoryAuthorizationCodeServices authInMemoryAuthorizationCodeServices;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsServiceBuilder inMemoryClientDetailsServiceBuilder = clients.inMemory();
        for (OAuthClientDetail oauthClientDetail : oauthClientDetailsConfig.getClients()) {
            String clientId = oauthClientDetail.getClientId();
            String clientSecret = oauthClientDetail.getClientSecret();
            String clientUrl = oauthClientDetail.getClientUrl();
            String encodedClientSecret = passwordEncoder.encode(clientSecret);
            inMemoryClientDetailsServiceBuilder.withClient(clientId)
                .authorizedGrantTypes("authorization_code")
                .scopes("all")
                .secret(encodedClientSecret)
                .redirectUris(clientUrl + "/login")
                .accessTokenValiditySeconds(3600)
                .autoApprove(true);
        }
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.realm("simple").tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()")
            .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        enhancerChain.setTokenEnhancers(Arrays.asList(authServerTokenEnhancer(), accessTokenConverter()));
        endpoints.tokenStore(jwtTokenStore()).accessTokenConverter(accessTokenConverter())
            .userDetailsService(mecUserDetailsService).tokenEnhancer(enhancerChain);
        endpoints.authorizationCodeServices(authInMemoryAuthorizationCodeServices);
    }

    /**
     * jwt access token converter.
     *
     * @return
     */

    @Bean
    public JwtAccessTokenConverter accessTokenConverter()
        throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        KeyPair keyPair;
        if (StringUtils.isEmpty(publicKeyStr) || StringUtils.isEmpty(encryptedPrivateKey) || StringUtils
            .isEmpty(encryptPasswordStr)) {
            KeyPairGenerator keyPairGenerator = null;
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPair = keyPairGenerator.generateKeyPair();
        } else {
            publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");
            encryptedPrivateKey = encryptedPrivateKey.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "")
                .replace("-----END ENCRYPTED PRIVATE KEY-----", "");
            byte[] publicKeyBytes = new Base64().decode(publicKeyStr);
            byte[] encryptedPrivateKeyBytes = new Base64().decode(encryptedPrivateKey);

            EncryptedPrivateKeyInfo pkInfo = new EncryptedPrivateKeyInfo(encryptedPrivateKeyBytes);
            PBEKeySpec keySpec = new PBEKeySpec(encryptPasswordStr.toCharArray());
            SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance(pkInfo.getAlgName());
            PKCS8EncodedKeySpec privateKeySpec = pkInfo.getKeySpec(pbeKeyFactory.generateSecret(keySpec));

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            keyPair = new KeyPair(publicKey, privateKey);
        }
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Bean
    public JwtTokenStore jwtTokenStore()
        throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public TokenEnhancer authServerTokenEnhancer() {
        return new AuthServerTokenEnhancer();
    }
}
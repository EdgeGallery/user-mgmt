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

package org.edgegallery.user.auth.config.security;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.edgegallery.user.auth.config.OAuthClientDetail;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.utils.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
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
    private MecUserDetailsServiceImpl mecUserDetailsService;

    @Autowired
    private AuthInMemoryAuthorizationCodeServices authInMemoryAuthorizationCodeServices;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        InMemoryClientDetailsServiceBuilder inMemoryClientDetailsServiceBuilder = clients.inMemory();
        for (OAuthClientDetail oauthClientDetail : oauthClientDetailsConfig.getEnabledClients()) {
            String clientId = oauthClientDetail.getClientId();
            String clientSecret = oauthClientDetail.getClientSecret();
            String clientUrl = oauthClientDetail.getClientAccessUrl();
            String encodedClientSecret = passwordEncoder.encode(clientSecret);
            inMemoryClientDetailsServiceBuilder.withClient(clientId)
                .authorizedGrantTypes(Consts.GRANT_TYPE)
                .scopes("all")
                .secret(encodedClientSecret)
                .redirectUris(clientUrl + "/login")
                .accessTokenValiditySeconds(Consts.SECOND_HALF_DAY)
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
        endpoints.tokenGranter(tokenGranter(endpoints));
    }

    /**
     * jwt access token converter.
     *
     * @return
     */
    @Bean
    public JwtAccessTokenConverter accessTokenConverter()
        throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(getKeyPair());
        return converter;
    }

    /**
     * key pair.
     *
     * @return Key Pair
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws InvalidKeySpecException InvalidKeySpecException
     * @throws IOException IOException
     * @throws InvalidKeyException InvalidKeyException
     */
    @Bean
    public KeyPair getKeyPair()
        throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        KeyPair keyPair;
        if (StringUtils.isEmpty(publicKeyStr) || StringUtils.isEmpty(encryptedPrivateKey) || StringUtils
            .isEmpty(encryptPasswordStr)) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
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
        return keyPair;
    }

    /**
     * get jwt token store.
     *
     * @return jwt token store
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws IOException IOException
     * @throws InvalidKeySpecException InvalidKeySpecException
     * @throws InvalidKeyException InvalidKeyException
     */
    @Bean
    public JwtTokenStore jwtTokenStore()
        throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException {
        return new JwtTokenStore(accessTokenConverter());
    }

    /**
     * get auth server token enhancer.
     *
     * @return token enhancer
     */
    @Bean
    public TokenEnhancer authServerTokenEnhancer() {
        return new AuthServerTokenEnhancer();
    }

    /**
     * get token granter.
     *
     * @param endpoints endpoints
     * @return token granter
     */
    @Bean
    public TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        return new TokenGranter() {
            private CompositeTokenGranter delegate;

            public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
                if (this.delegate == null) {
                    this.delegate = new CompositeTokenGranter(loadDefaultTokenGranters(endpoints));
                }

                return this.delegate.grant(grantType, tokenRequest);
            }
        };
    }

    private List<TokenGranter> loadDefaultTokenGranters(AuthorizationServerEndpointsConfigurer endpoints) {
        AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
        AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();
        ClientDetailsService clientDetailsService = endpoints.getClientDetailsService();
        OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

        List<TokenGranter> tokenGranters = new ArrayList<>();
        tokenGranters.add(new ExtendAuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices,
            clientDetailsService, requestFactory));
        return tokenGranters;
    }
}
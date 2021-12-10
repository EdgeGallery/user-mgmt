/*
 *  Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.user.auth.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.config.security.ExtendAuthorizationCodeTokenGranter;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.edgegallery.user.auth.utils.Consts;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class OAuthTokenTest {

    private static final String TEST_REDIRECT_URI = "test_redirect_uri";

    private static final String TEST_CLIENT_ID = "test";

    @Autowired
    private TokenGranter tokenGranter;

    @Autowired
    private TokenEnhancer authServerTokenEnhancer;

    @Before
    public void begin() {
    }

    @After
    public void end() {
    }

    @Test
    public void grantToken() {
        new MockUp<ExtendAuthorizationCodeTokenGranter>() {
            @Mock
            private OAuth2Authentication getStoredAuth(String authorizationCode) {
                return buildOAuth2Authentication(false);
            }
        };

        TokenRequest tokenReq = new TokenRequest(buildReqParameters(), TEST_CLIENT_ID, Collections.emptyList(),
            Consts.GRANT_TYPE);
        OAuth2AccessToken oAuth2AccessToken = tokenGranter.grant(Consts.GRANT_TYPE, tokenReq);
        Assert.assertNotNull(oAuth2AccessToken);
    }

    @Test
    public void tokenEnhance() {
        OAuth2AccessToken result = authServerTokenEnhancer
            .enhance(buildOAuth2AccessToken(), buildOAuth2Authentication(false));
        Assert.assertNotNull(result);
    }

    @Test
    public void tokenEnhance_externaluser() {
        OAuth2AccessToken result = authServerTokenEnhancer
            .enhance(buildOAuth2AccessToken(), buildOAuth2Authentication(true));
        Assert.assertNotNull(result);
    }

    private OAuth2AccessToken buildOAuth2AccessToken() {
        OAuth2AccessToken oAuth2AccessToken = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
        return oAuth2AccessToken;
    }

    private OAuth2Authentication buildOAuth2Authentication(boolean buildExternalUser) {
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(buildOAuth2Request(),
            buildUserAuthentication(buildExternalUser));
        return oAuth2Authentication;
    }

    private OAuth2Request buildOAuth2Request() {
        return new OAuth2Request(buildReqParameters(), TEST_CLIENT_ID, Collections.emptyList(), true,
            Collections.emptySet(), Collections.emptySet(), TEST_REDIRECT_URI, Collections.emptySet(),
            Collections.emptyMap());
    }

    private Map<String, String> buildReqParameters() {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("code", "xxx");
        requestParameters.put("grant_type", Consts.GRANT_TYPE);
        requestParameters.put("response_type", "code");
        requestParameters.put("redirect_uri", TEST_REDIRECT_URI);

        return requestParameters;
    }

    private Authentication buildUserAuthentication(boolean buildExternalUser) {
        String userName = buildExternalUser ? new ExternalUser("userid-test", "username-test", "user-test@s.c",
            EnumRole.TENANT.name()).build() : Consts.SUPER_ADMIN_NAME;
        User principal = new User(userName, "", true, true, true, true, Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(principal, null);
    }
}

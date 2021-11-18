/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

import java.util.HashMap;
import java.util.Map;
import org.edgegallery.user.auth.utils.Consts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

/**
 * Extend Auth Code Token Granter.
 */
public class ExtendAuthorizationCodeTokenGranter extends AbstractTokenGranter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final AuthorizationCodeServices authorizationCodeServices;

    public ExtendAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices,
        AuthorizationCodeServices authorizationCodeServices, ClientDetailsService clientDetailsService,
        OAuth2RequestFactory requestFactory) {
        this(tokenServices, authorizationCodeServices, clientDetailsService, requestFactory, Consts.GRANT_TYPE);
    }

    protected ExtendAuthorizationCodeTokenGranter(AuthorizationServerTokenServices tokenServices,
        AuthorizationCodeServices authorizationCodeServices, ClientDetailsService clientDetailsService,
        OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authorizationCodeServices = authorizationCodeServices;
    }

    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String authorizationCode = (String) parameters.get("code");
        String redirectUri = (String) parameters.get("redirect_uri");
        if (authorizationCode == null) {
            throw new InvalidRequestException("An authorization code must be supplied.");
        }

        OAuth2Authentication storedAuth = this.authorizationCodeServices.consumeAuthorizationCode(authorizationCode);
        if (storedAuth == null) {
            throw new InvalidGrantException("Invalid authorization code: " + authorizationCode);
        }

        OAuth2Request pendingOAuth2Request = storedAuth.getOAuth2Request();
        String redirectUriApprovalParameter = (String) pendingOAuth2Request.getRequestParameters().get("redirect_uri");
        if ((redirectUri != null || redirectUriApprovalParameter != null) && !pendingOAuth2Request.getRedirectUri()
            .equals(redirectUri)) {
            LOGGER.warn("Redirect URI mismatch, ignore it.");
        }

        String pendingClientId = pendingOAuth2Request.getClientId();
        String clientId = tokenRequest.getClientId();
        if (clientId != null && !clientId.equals(pendingClientId)) {
            throw new InvalidClientException("Client ID mismatch");
        }

        Map<String, String> combinedParameters = new HashMap(pendingOAuth2Request.getRequestParameters());
        combinedParameters.putAll(parameters);
        OAuth2Request finalStoredOAuth2Request = pendingOAuth2Request.createOAuth2Request(combinedParameters);
        Authentication userAuth = storedAuth.getUserAuthentication();
        return new OAuth2Authentication(finalStoredOAuth2Request, userAuth);
    }
}

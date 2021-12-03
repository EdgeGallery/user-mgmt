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

package org.edgegallery.user.auth.config.security;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class AuthServerTokenEnhancer implements TokenEnhancer {

    @Autowired
    private MecUserDetailsServiceImpl mecUserDetailsService;

    @Autowired
    private TenantPoMapper tenantPoMapper;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SmsConfig smsConfig;

    @Value("${mail.enabled}")
    private String mailEnabled;

    @Value("${external.iam.enabled}")
    private boolean externalIamEnabled;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oauth2AccessToken, OAuth2Authentication oauth2Authentication) {
        User user = (User) oauth2Authentication.getPrincipal();
        Map<String, Object> additionalMap = new HashMap<>();
        boolean isExternalUser = ExternalUserUtil.isExternalUser(user.getUsername());
        if (isExternalUser) {
            ExternalUser externalUser = new ExternalUser();
            externalUser.parse(user.getUsername());
            additionalMap.put("userId", externalUser.getUserId());
            additionalMap.put("userName", externalUser.getUserName());
        } else {
            TenantPo tenant = tenantPoMapper.getTenantByUsername(user.getUsername());
            additionalMap.put("userId", tenant != null ? tenant.getTenantId() : null);
            additionalMap.put("userName", user.getUsername());
            if (mecUserDetailsService.getPwModifyScene(user.getUsername()) > 0) {
                additionalMap.put("pwmodiscene", null);
            }
        }

        additionalMap.put("enableSms", externalIamEnabled ? Boolean.FALSE : smsConfig.getEnabled());
        additionalMap.put("enableMail", externalIamEnabled ? Boolean.FALSE : mailEnabled);
        additionalMap.put("ssoSessionId", request.getServletContext()
            .getAttribute(oauth2Authentication.getOAuth2Request().getRequestParameters().get("code")));
        additionalMap.put("enableExternalIam", externalIamEnabled);

        ((DefaultOAuth2AccessToken) oauth2AccessToken).setAdditionalInformation(additionalMap);
        return oauth2AccessToken;
    }
}
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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.edgegallery.user.auth.utils.Consts;
import org.edgegallery.user.auth.utils.UserLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private static final String HEADER_KEY_PW_MODIFY_SCENE = "pwmodiscene";

    @Autowired
    private MecUserDetailsServiceImpl mecUserDetailsService;

    @Autowired
    private TenantPoMapper tenantPoMapper;

    @Autowired
    private UserLockUtil userLockUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        LOGGER.info("login success.");

        String userName = authentication.getName();
        boolean isExternalUser = ExternalUserUtil.isExternalUser(userName);
        if (isExternalUser) {
            clearExternalUserLock(userName);
        } else {
            clearInnerUserLock(userName);
        }

        if (userName.equalsIgnoreCase(Consts.GUEST_USER_NAME)) {
            String redirectUrl = getRedirectUrl(request, response);
            if (redirectUrl != null) {
                redirectUrl = redirectUrl.replace("login", "#/index");
                response.sendRedirect(redirectUrl);
            }
        } else {
            if (!isExternalUser) {
                int pwModiScene = mecUserDetailsService.getPwModifyScene(userName);
                if (pwModiScene > 0) {
                    response.addIntHeader(HEADER_KEY_PW_MODIFY_SCENE, pwModiScene);
                }
            }
        }
    }

    private void clearInnerUserLock(String userName) {
        userLockUtil.clearFailedCount(userName);

        TenantPo tenant = tenantPoMapper.getTenantByUniqueFlag(userName);
        if (tenant == null) {
            LOGGER.error("user login succeeded but not found, it is abnormal.");
            return;
        }

        if (!StringUtils.isEmpty(tenant.getMailAddress())) {
            userLockUtil.clearFailedCount(tenant.getMailAddress());
        }
        
        if (!StringUtils.isEmpty(tenant.getTelephoneNumber())) {
            userLockUtil.clearFailedCount(tenant.getTelephoneNumber());
        }
    }

    private void clearExternalUserLock(String userName) {
        ExternalUser externalUser = new ExternalUser();
        externalUser.parse(userName);

        if (!StringUtils.isEmpty(externalUser.getUserName())) {
            userLockUtil.clearFailedCount(externalUser.getUserName());
        }

        if (!StringUtils.isEmpty(externalUser.getMailAddress())) {
            userLockUtil.clearFailedCount(externalUser.getMailAddress());
        }
    }

    private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        RequestCache cache = new HttpSessionRequestCache();
        SavedRequest savedRequest = cache.getRequest(request, response);
        if (savedRequest == null) {
            return null;
        }
        String url = savedRequest.getRedirectUrl();
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        String[] urlArray = url.split("\\?");
        if (urlArray.length != 2) {
            return null;
        }
        String[] parameters = urlArray[1].split("&");
        String redirectUrl = null;
        for (String parameter : parameters) {
            String[] keyValue = parameter.split("=");
            if (keyValue.length == 2 && keyValue[0].equalsIgnoreCase("redirect_uri")) {
                redirectUrl = keyValue[1];
                break;
            }
        }
        return redirectUrl;
    }
}
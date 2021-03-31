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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class OAuthUnauthorizedEntryPoint extends LoginUrlAuthenticationEntryPoint {
    private String enableSmsStr;

    private String enableMailStr;

    /**
     * constructor.
     *
     * @param loginFormUrl login form url
     * @param enableSmsStr enable sms flag
     * @param enableMailStr enable mail flag
     */
    public OAuthUnauthorizedEntryPoint(String loginFormUrl, String enableSmsStr, String enableMailStr) {
        super(loginFormUrl);
        this.enableSmsStr = enableSmsStr;
        this.enableMailStr = enableMailStr;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String queryString = request.getQueryString();
        if (queryString != null && queryString.contains("redirect_uri")) {
            String[] params = queryString.split("&");
            for (String param : params) {
                if (param.contains("redirect_uri")) {
                    String returnToUrl = param.replace("redirect_uri=", "").replace("/login", "");
                    request.getSession().setAttribute("return_to", returnToUrl);
                    request.getSession().setAttribute("enableSms", enableSmsStr);
                    request.getSession().setAttribute("enableMail", enableMailStr);
                    break;
                }
            }
        }
        OAuthRedirectStrategy oauthRedirectStrategy = new OAuthRedirectStrategy();
        String redirectUrl = buildRedirectUrlToLoginPage(request, response, authException);
        oauthRedirectStrategy.sendRedirect(request, response, redirectUrl);
    }
}
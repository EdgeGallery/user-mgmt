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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.DefaultRedirectStrategy;

public class OAuthRedirectStrategy extends DefaultRedirectStrategy {
    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        Object returnToAttr = request.getSession().getAttribute("return_to");
        Object enableSms = request.getSession().getAttribute("enableSms");
        if (returnToAttr != null && enableSms != null) {
            redirectUrl += "?return_to=" + returnToAttr.toString();
            redirectUrl += "&enable_sms=" + enableSms.toString();
        }
        response.sendRedirect(redirectUrl);
    }
}

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

import org.edgegallery.user.auth.utils.UserLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    @Autowired
    private UserLockUtil userLockUtil;

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        if (event.getException().getClass().equals(BadCredentialsException.class)) {
            // when login failed, the getName of Authentication is original login credential: user name or email or telephone
            String userFlag = event.getAuthentication().getName();
            userLockUtil.addFailedCount(userFlag);
        }
    }
}

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

package org.edgegallery.user.auth.external.iam.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.external.iam.IExternalIamLogin;
import org.edgegallery.user.auth.external.iam.IExternalIamService;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalIamLoginImpl implements IExternalIamLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalIamLoginImpl.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    @Autowired
    private IExternalIamService externalIamService;

    @Override
    public UserDetails loadUser(String userFlag, String password) {
        LOGGER.info("load user from external iam.");
        ExternalUser externalUser = externalIamService.login(userFlag, password);
        if (externalUser == null) {
            LOGGER.error("external login failed.");
            throw new UsernameNotFoundException("User not found: " + userFlag);
        }

        EnumRole userRole = ExternalUserUtil.convertUserRole(externalUser.getUserRole());
        List<GrantedAuthority> authorities = Arrays.stream(EnumPlatform.values())
            .map(plat -> new SimpleGrantedAuthority("ROLE_" + plat + "_" + userRole.toString()))
            .collect(Collectors.toList());
        return new User(externalUser.build(), passwordEncoder.encode(password), true, true, true, true,
            authorities);
    }

}

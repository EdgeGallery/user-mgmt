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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ClientUserBean {

    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    private static int CLIENT_LOGIN_TIMEOUT = 5000;

    private List<RolePo> clientDefaultRoles() {
        List<RolePo> roles = new ArrayList<>();
        for (EnumPlatform plat : EnumPlatform.values()) {
            roles.add(new RolePo(plat, EnumRole.TENANT));
        }
        return roles;
    }

    public User parserClientUser(String userName) {
        final TenantPo clientUser = new TenantPo();
        String[] userNameArr = userName.split(":");
        if (userNameArr.length != 2) {
            return null;
        }
        final String inClientId = userNameArr[0];
        String inTime = userNameArr[1];
        if (new Date().getTime() - Long.valueOf(inTime) > CLIENT_LOGIN_TIMEOUT) {
            return null;
        }
        oauthClientDetailsConfig.getClients().forEach(clientDetail -> {
            String clientId = clientDetail.getClientId();
            if (inClientId.equalsIgnoreCase(clientId)) {
                String secret = clientDetail.getClientSecret();
                // passwordEncoder.encode(secret);
                clientUser.setUsername(clientId);
                clientUser.setPassword(passwordEncoder.encode(secret));
                return;
            }
        });
        List<RolePo> rolePos = clientDefaultRoles();
        List<GrantedAuthority> authorities = new ArrayList<>();
        rolePos.forEach(rolePo -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rolePo.toString())));
        return new User(clientUser.getUsername(), clientUser.getPassword(), true, true, true, true, authorities);
    }
}

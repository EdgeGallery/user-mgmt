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

package org.mec.auth.server.config.security;

import java.util.ArrayList;
import java.util.List;
import org.mec.auth.server.db.entity.RolePo;
import org.mec.auth.server.db.entity.TenantPo;
import org.mec.auth.server.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class MecUserDetailsService implements UserDetailsService {

    @Autowired
    TenantPoMapper tenantPoMapper;

    @Override
    public UserDetails loadUserByUsername(String userNameOrTelephoneNum) throws UsernameNotFoundException {
        TenantPo tenant = tenantPoMapper.getTenantByUsername(userNameOrTelephoneNum);
        if (tenant == null) {
            tenant = tenantPoMapper.getTenantByTelephone(userNameOrTelephoneNum);
            if (tenant == null) {
                throw new UsernameNotFoundException(
                    "Can't find user by userNameOrTelephoneNum:" + userNameOrTelephoneNum);
            }
        }
        List<RolePo> rolePos = tenantPoMapper.getRolePoByTenantId(tenant.getTenantId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        rolePos.forEach(rolePo -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rolePo.toString())));
        return new User(tenant.getTenantId(), tenant.getPassword(), authorities);
    }
}

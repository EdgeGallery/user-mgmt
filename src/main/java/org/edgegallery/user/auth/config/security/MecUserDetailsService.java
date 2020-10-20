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

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSuccessHandler.class);

    @Autowired
    private TenantPoMapper tenantPoMapper;

    private Set<RequestLimitRule> rules = Collections.singleton(RequestLimitRule.of(Duration.ofMinutes(5), 3));
    private RequestRateLimiter limiter = new InMemorySlidingWindowRequestRateLimiter(rules);

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

        boolean isOverLimit = isOverLimit(userNameOrTelephoneNum);
        User user = new User(tenant.getTenantId(), tenant.getPassword(), true,
            true, true, !isOverLimit, authorities);
        if (isOverLimit) {
            LOGGER.info("user has been locked, username: {}", userNameOrTelephoneNum);
        }
        return user;
    }

    public void addFailedCount(String userId) {
        limiter.overLimitWhenIncremented(userId);
    }

    public boolean isOverLimit(String userId) {
        return limiter.geLimitWhenIncremented(userId, 0);
    }

    public void clearFailedCount(String userId) {
        limiter.resetLimit(userId);
    }
}

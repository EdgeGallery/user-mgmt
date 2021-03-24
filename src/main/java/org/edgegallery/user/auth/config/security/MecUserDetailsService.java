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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MecUserDetailsService.class);

    // when login failed 5 times, account will be locked.
    private static final Set<RequestLimitRule> rules = Collections
        .singleton(RequestLimitRule.of(Duration.ofMinutes(5), 4));

    // locked overtime
    private static final long OVERTIME = 5 * 60 * 1000L;

    private static final RequestRateLimiter LIMITER = new InMemorySlidingWindowRequestRateLimiter(rules);

    private static final Map<String, Long> LOCKED_USERS_MAP = new HashMap<>();

    @Autowired
    private TenantPoMapper tenantPoMapper;

    @Autowired
    private ClientUserBean clientUserBean;


    @Override
    public UserDetails loadUserByUsername(String uniqueUserFlag) throws UsernameNotFoundException {
        TenantPo tenant = tenantPoMapper.getTenantByUniqueFlag(uniqueUserFlag);
        if (tenant == null || !tenant.isAllowed()) {
            // to check client user
            User user = clientUserBean.parserClientUser(uniqueUserFlag);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + uniqueUserFlag);
            } else {
                return user;
            }
        }
        List<RolePo> rolePos = tenantPoMapper.getRolePoByTenantId(tenant.getTenantId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        rolePos.forEach(rolePo -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rolePo.toString())));
        boolean isLocked = isLocked(uniqueUserFlag);
        if (isLocked) {
            LOGGER.info("username:{} have been locked.", tenant.getUsername());
        }
        return new User(tenant.getUsername(), tenant.getPassword(), true, true, true, !isLocked, authorities);
    }

    private boolean isLocked(String userId) {
        if (LOCKED_USERS_MAP.containsKey(userId)) {
            long lockedTime = LOCKED_USERS_MAP.get(userId);
            if (System.currentTimeMillis() - lockedTime < OVERTIME) {
                return true;
            } else {
                LOCKED_USERS_MAP.remove(userId);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * when login failed.
     */
    public void addFailedCount(String userId) {
        boolean isOver = LIMITER.overLimitWhenIncremented(userId);
        if (isOver) {
            LOCKED_USERS_MAP.put(userId, System.currentTimeMillis());
        }
    }

    /**
     * when login success.
     */
    public void clearFailedCount(String userId) {
        LIMITER.resetLimit(userId);
    }
}

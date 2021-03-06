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

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.user.auth.config.OAuthClientDetail;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.service.IdentityService;
import org.edgegallery.user.auth.utils.Consts;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;

@Component
public class MecUserDetailsService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MecUserDetailsService.class);

    // when login failed 5 times, account will be locked.
    private static final Set<RequestLimitRule> rules = Collections
        .singleton(RequestLimitRule.of(Duration.ofMinutes(5), 4));

    // locked overtime
    private static final long OVERTIME = 5 * 60 * 1000L;

    private static final int CLIENT_LOGIN_TIMEOUT = 5000;

    private static final RequestRateLimiter LIMITER = new InMemorySlidingWindowRequestRateLimiter(rules);

    private static final Map<String, Long> LOCKED_USERS_MAP = new HashMap<>();

    @Autowired
    private TenantPoMapper tenantPoMapper;

    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdentityService identityService;

    @Value("${secPolicy.pwTimeout}")
    private int pwTimeout;

    @Override
    public UserDetails loadUserByUsername(String uniqueUserFlag) throws UsernameNotFoundException {
        checkVerificationCode(uniqueUserFlag);
        TenantPo tenant = tenantPoMapper.getTenantByUniqueFlag(uniqueUserFlag);
        if (tenant == null) {
            // to check client user
            User user = parserClientUser(uniqueUserFlag);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + uniqueUserFlag);
            } else {
                return user;
            }
        } else if (!tenant.isAllowed()) {
            throw new UsernameNotFoundException("User not found: " + uniqueUserFlag);
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

    private void checkVerificationCode(String uniqueUserFlag) {
        if (Consts.GUEST_USER_NAME.equalsIgnoreCase(uniqueUserFlag)) {
            LOGGER.debug("guest login, no need check verification code.");
            return;
        }

        if (oauthClientDetailsConfig.getClients().stream()
            .anyMatch(clientDetail -> uniqueUserFlag.startsWith(clientDetail.getClientId() + ":"))) {
            LOGGER.debug("inner client login, no need check verification code.");
            return;
        }

        String verificationCode = ServletRequestUtils.getStringParameter(request, "verifyCode", "");
        if (!identityService.checkVerificatinCode(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE,
            request.getSession().getId(), verificationCode)) {
            LOGGER.error("invalid verification code.");
            throw new InternalAuthenticationServiceException(ErrorEnum.VERIFY_CODE_ERROR.message());
        }
    }

    private List<RolePo> clientDefaultRoles() {
        List<RolePo> roles = new ArrayList<>();
        for (EnumPlatform plat : EnumPlatform.values()) {
            roles.add(new RolePo(plat, EnumRole.TENANT));
        }
        return roles;
    }

    /**
     * to parse the username if this is a client user, and return this user.
     *
     * @param userName input username
     * @return User
     */
    public User parserClientUser(String userName) {
        final TenantPo clientUser = new TenantPo();
        String[] userNameArr = userName.split(":");
        if (userNameArr.length != 2) {
            return null;
        }
        final String inClientId = userNameArr[0];
        String inTime = userNameArr[1];
        if (new Date().getTime() - Long.parseLong(inTime) > CLIENT_LOGIN_TIMEOUT) {
            return null;
        }
        Optional<OAuthClientDetail> client = oauthClientDetailsConfig.getClients().stream()
            .filter(clientDetail -> inClientId.equalsIgnoreCase(clientDetail.getClientId())).findFirst();
        if (client.isPresent()) {
            OAuthClientDetail clientDetail = client.get();
            String secret = clientDetail.getClientSecret();
            clientUser.setUsername(clientDetail.getClientId());
            clientUser.setPassword(passwordEncoder.encode(secret));
        } else {
            return null;
        }
        List<RolePo> rolePos = clientDefaultRoles();
        List<GrantedAuthority> authorities = new ArrayList<>();
        rolePos.forEach(rolePo -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rolePo.toString())));
        return new User(clientUser.getUsername(), clientUser.getPassword(), true, true, true, true, authorities);
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

    /**
     * get pw modify scene.
     *
     * @param userName User Name
     * @return pw modify scene
     */
    public int getPwModifyScene(String userName) {
        String pwEffectTime = tenantPoMapper.getPwEffectTime(userName);
        if (StringUtils.isEmpty(pwEffectTime)) {
            return -1;
        }

        try {
            Date pwEffectDate = new SimpleDateFormat(Consts.DATE_PATTERN).parse(pwEffectTime);
            long passedDayCount = (System.currentTimeMillis() - pwEffectDate.getTime()) / Consts.MILLIS_ONE_DAY;
            if (passedDayCount > Consts.FIRST_LOGIN_JUDGE_DAYCOUNT) {
                return Consts.PwModifyScene.FIRSTLOGIN;
            } else if (passedDayCount > pwTimeout) {
                return Consts.PwModifyScene.EXPIRED;
            } else {
                LOGGER.debug("pw has not expired.");
                return -1;
            }
        } catch (ParseException e) {
            LOGGER.error("pw effect time parse failed! pwEffectTime = {}", pwEffectTime);
        }

        return -1;
    }
}

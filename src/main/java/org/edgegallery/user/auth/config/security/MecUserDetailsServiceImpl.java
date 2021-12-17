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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.edgegallery.user.auth.config.OAuthClientDetail;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.external.iam.IExternalIamLogin;
import org.edgegallery.user.auth.service.IdentityService;
import org.edgegallery.user.auth.utils.CommonUtil;
import org.edgegallery.user.auth.utils.Consts;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.edgegallery.user.auth.utils.UserLockUtil;
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
public class MecUserDetailsServiceImpl implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MecUserDetailsServiceImpl.class);

    private static final int CLIENT_LOGIN_TIMEOUT = 5000;

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

    @Autowired
    private UserLockUtil userLockUtil;

    @Value("${secPolicy.pwTimeout}")
    private int pwTimeout;

    @Value("${external.iam.enabled}")
    private boolean externalIamEnabled;

    @Autowired
    private IExternalIamLogin externalIamLogin;

    @Override
    public UserDetails loadUserByUsername(String uniqueUserFlag) throws UsernameNotFoundException {
        if (oauthClientDetailsConfig.getEnabledClients().stream()
            .anyMatch(clientDetail -> uniqueUserFlag.startsWith(clientDetail.getClientId() + ":"))) {
            LOGGER.debug("inner client login, parse client user.");
            User user = parseClientUser(uniqueUserFlag);
            if (user == null) {
                LOGGER.error("inner client login failed, client is {}", uniqueUserFlag);
                throw new UsernameNotFoundException("User not found: " + uniqueUserFlag);
            }

            return user;
        }

        checkVerificationCode(uniqueUserFlag);
        if (!externalIamEnabled || CommonUtil.isInnerDefaultUser(uniqueUserFlag)) {
            return loadInnerUser(uniqueUserFlag);
        } else {
            return externalIamLogin.loadUser(uniqueUserFlag, request.getParameter("password"));
        }
    }

    private User loadInnerUser(String uniqueUserFlag) {
        TenantPo tenant = tenantPoMapper.getTenantByUniqueFlag(uniqueUserFlag);
        if (tenant == null || !tenant.isAllowed()) {
            String errorContent = "User not found: " + uniqueUserFlag;
            LOGGER.error(errorContent);
            throw new UsernameNotFoundException(errorContent);
        }
        List<RolePo> rolePos = tenantPoMapper.getRolePoByTenantId(tenant.getTenantId());
        List<GrantedAuthority> authorities = rolePos.stream()
            .map(rolePo -> new SimpleGrantedAuthority("ROLE_" + rolePo.toString())).collect(Collectors.toList());
        boolean isLocked = userLockUtil.isLocked(uniqueUserFlag);
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

        String verificationCode = ServletRequestUtils.getStringParameter(request, "verifyCode", "");
        if (!identityService.checkVerificatinCode(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE,
            request.getSession().getId(), verificationCode)) {
            LOGGER.error("invalid verification code.");
            throw new InternalAuthenticationServiceException(ErrorEnum.VERIFY_CODE_ERROR.message());
        }
    }

    /**
     * parse the username if this is a client user, and return this user.
     *
     * @param userName input username
     * @return User
     */
    public User parseClientUser(String userName) {
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
        Optional<OAuthClientDetail> client = oauthClientDetailsConfig.getEnabledClients().stream()
            .filter(clientDetail -> inClientId.equalsIgnoreCase(clientDetail.getClientId())).findFirst();
        if (!client.isPresent()) {
            LOGGER.error("client not found.");
            return null;
        }

        OAuthClientDetail clientDetail = client.get();
        String secret = clientDetail.getClientSecret();
        clientUser.setUsername(clientDetail.getClientId());
        clientUser.setPassword(passwordEncoder.encode(secret));
        List<GrantedAuthority> authorities = Arrays.stream(EnumPlatform.values())
            .map(plat -> new SimpleGrantedAuthority("ROLE_" + plat + "_TENANT")).collect(Collectors.toList());
        return new User(clientUser.getUsername(), clientUser.getPassword(), true, true, true, true, authorities);
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

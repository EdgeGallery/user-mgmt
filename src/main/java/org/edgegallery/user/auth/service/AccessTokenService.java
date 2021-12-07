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

package org.edgegallery.user.auth.service;

import fj.data.Either;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.user.auth.config.validate.annotation.ParameterValidate;
import org.edgegallery.user.auth.controller.dto.request.GetAccessTokenReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.GetAccessTokenRespDto;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.external.iam.IExternalIamService;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.edgegallery.user.auth.utils.CommonUtil;
import org.edgegallery.user.auth.utils.Consts;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.edgegallery.user.auth.utils.UserLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.stereotype.Service;

@Service("accessTokenService")
public class AccessTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenService.class);

    @Autowired
    private TenantPoMapper tenantPoMapper;

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    @Autowired
    private UserLockUtil userLockUtil;

    @Autowired
    private KeyPair keyPair;

    @Value("${external.iam.enabled}")
    private boolean externalIamEnabled;

    @Autowired
    private IExternalIamService externalIamService;

    private JsonParser jsonParser = JsonParserFactory.create();

    /**
     * get access token.
     *
     * @param getAccessTokenReqDto getAccessTokenReqDto
     * @return Access Token Result or Error
     */
    @ParameterValidate
    public Either<GetAccessTokenRespDto, FormatRespDto> getAccessToken(GetAccessTokenReqDto getAccessTokenReqDto) {
        String userFlag = getAccessTokenReqDto.getUserFlag();
        LOGGER.info("get access token in service. userFlag = {}", userFlag);
        if (userLockUtil.isLocked(userFlag)) {
            LOGGER.error("user has locked.");
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.USER_LOCKED)));
        }

        Map<String, Object> userInfoMap = null;
        if (!externalIamEnabled || CommonUtil.isInnerDefaultUser(userFlag)) {
            TenantPo user = tenantPoMapper.getTenantByUniqueFlag(userFlag);
            if (user == null || !user.isAllowed()) {
                LOGGER.error("user {} not found or not allowed.", userFlag);
                userLockUtil.addFailedCount(userFlag);
                return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
            }

            if (!passwordEncoder.matches(getAccessTokenReqDto.getPassword(), user.getPassword())) {
                LOGGER.error("password incorrect.");
                userLockUtil.addFailedCount(userFlag);
                return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
            }

            List<RolePo> rolePos = tenantPoMapper.getRolePoByTenantId(user.getTenantId());
            userInfoMap = buildUserInfoMap(user, rolePos);
        } else {
            ExternalUser externalUser = externalIamService.login(userFlag, getAccessTokenReqDto.getPassword());
            if (externalUser == null) {
                LOGGER.error("external login failed.");
                userLockUtil.addFailedCount(userFlag);
                return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
            }

            userInfoMap = buildUserInfoMap(externalUser);
        }

        LOGGER.info("generate access token.");
        String accessToken = generateAccessToken(userInfoMap);

        LOGGER.info("get access token succeed! userFlag = {}", userFlag);
        userLockUtil.clearFailedCount(userFlag);
        return Either.left(new GetAccessTokenRespDto(accessToken));
    }

    private Map<String, Object> buildUserInfoMap(TenantPo user, List<RolePo> rolePos) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("user_name", user.getUsername());
        dataMap.put("userName", user.getUsername());
        dataMap.put("userId", user.getTenantId());
        dataMap.put("authorities",
            rolePos.stream().map(rolePo -> "ROLE_" + rolePo.toString()).collect(Collectors.toList()));
        dataMap.put("jti", UUID.randomUUID().toString());

        dataMap.put("exp", System.currentTimeMillis() / 1000 + Consts.SECOND_HALF_DAY);
        dataMap.put("scope", Arrays.asList("all"));

        return dataMap;
    }

    private Map<String, Object> buildUserInfoMap(ExternalUser externalUser) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("user_name", externalUser.getUserName());
        dataMap.put("userName", externalUser.getUserName());
        dataMap.put("userId", externalUser.getUserId());

        EnumRole userRole = ExternalUserUtil.convertUserRole(externalUser.getUserRole());
        dataMap.put("authorities",
            Arrays.stream(EnumPlatform.values()).map(plat -> "ROLE_" + plat + "_" + userRole.toString())
                .collect(Collectors.toList()));
        dataMap.put("jti", UUID.randomUUID().toString());

        dataMap.put("exp", System.currentTimeMillis() / 1000 + Consts.SECOND_HALF_DAY);
        dataMap.put("scope", Arrays.asList("all"));

        return dataMap;
    }

    private String generateAccessToken(Map<String, Object> userInfoMap) {
        String content = jsonParser.formatMap(userInfoMap);
        PrivateKey privateKey = keyPair.getPrivate();
        Signer signer = new RsaSigner((RSAPrivateKey) privateKey);
        return JwtHelper.encode(content, signer).getEncoded();
    }
}

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.user.auth.config.OAuthClientDetail;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
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

    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

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
        if (!oauthClientDetailsConfig.getEnabledClients().stream()
            .anyMatch(clientDetail -> userFlag.startsWith(clientDetail.getClientId() + ":"))) {
            LOGGER.info("get access token. userFlag = {}", userFlag);
            return getAccessToken(userFlag, getAccessTokenReqDto.getPassword());
        } else {
            LOGGER.info("get access token for inner client user. userFlag = {}", userFlag);
            return getAccessTokenForClientUser(userFlag, getAccessTokenReqDto.getPassword());
        }
    }

    private Either<GetAccessTokenRespDto, FormatRespDto> getAccessToken(String userFlag, String password) {
        if (userLockUtil.isLocked(userFlag)) {
            LOGGER.error("user has locked.");
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.USER_LOCKED)));
        }

        Either<Map, FormatRespDto> userInfoMap = null;
        if (!externalIamEnabled || CommonUtil.isInnerDefaultUser(userFlag)) {
            userInfoMap = buildInnerUserInfoMap(userFlag, password);
        } else {
            userInfoMap = buildExternalUserInfoMap(userFlag, password);
        }

        if (userInfoMap.isRight()) {
            LOGGER.error("user info build failed.");
            return Either.right(userInfoMap.right().value());
        }

        LOGGER.info("generate access token.");
        String accessToken = generateAccessToken(userInfoMap.left().value());

        LOGGER.info("get access token succeed! userFlag = {}", userFlag);
        userLockUtil.clearFailedCount(userFlag);
        return Either.left(new GetAccessTokenRespDto(accessToken));
    }

    private Either<Map, FormatRespDto> buildInnerUserInfoMap(String userFlag, String password) {
        TenantPo user = tenantPoMapper.getTenantByUniqueFlag(userFlag);
        if (user == null || !user.isAllowed()) {
            LOGGER.error("user {} not found or not allowed.", userFlag);
            userLockUtil.addFailedCount(userFlag);
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            LOGGER.error("password incorrect.");
            userLockUtil.addFailedCount(userFlag);
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("user_name", user.getUsername());
        userInfoMap.put("userName", user.getUsername());
        userInfoMap.put("userId", user.getTenantId());

        List<RolePo> rolePos = tenantPoMapper.getRolePoByTenantId(user.getTenantId());
        userInfoMap.put("authorities",
            rolePos.stream().map(rolePo -> "ROLE_" + rolePo.toString()).collect(Collectors.toList()));

        enhanceCommonInfo(userInfoMap);
        return Either.left(userInfoMap);
    }

    private Either<Map, FormatRespDto> buildExternalUserInfoMap(String userFlag, String password) {
        ExternalUser externalUser = externalIamService.login(userFlag, password);
        if (externalUser == null) {
            LOGGER.error("external login failed.");
            userLockUtil.addFailedCount(userFlag);
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("user_name", externalUser.getUserName());
        userInfoMap.put("userName", externalUser.getUserName());
        userInfoMap.put("userId", externalUser.getUserId());

        EnumRole userRole = ExternalUserUtil.convertUserRole(externalUser.getUserRole());
        userInfoMap.put("authorities",
            Arrays.stream(EnumPlatform.values()).map(plat -> "ROLE_" + plat + "_" + userRole.toString())
                .collect(Collectors.toList()));

        enhanceCommonInfo(userInfoMap);
        return Either.left(userInfoMap);
    }

    private Either<GetAccessTokenRespDto, FormatRespDto> getAccessTokenForClientUser(String userFlag, String password) {
        String[] userNameArr = userFlag.split(":");
        if (userNameArr.length != 2) {
            LOGGER.error("inner client user {} not illegal.", userFlag);
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorRespDto.build(ErrorEnum.PARA_ILLEGAL)));
        }

        String inTime = userNameArr[1];
        if (new Date().getTime() - Long.parseLong(inTime) > Consts.CLIENT_LOGIN_TIMEOUT) {
            LOGGER.error("inner client user {} timeout.", userFlag);
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        String inClientId = userNameArr[0];
        Optional<OAuthClientDetail> client = oauthClientDetailsConfig.getEnabledClients().stream()
            .filter(clientDetail -> inClientId.equalsIgnoreCase(clientDetail.getClientId())).findFirst();
        if (!client.isPresent()) {
            LOGGER.error("inner client user {} not found.", userFlag);
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        OAuthClientDetail clientDetail = client.get();
        if (!passwordEncoder.matches(password, passwordEncoder.encode(clientDetail.getClientSecret()))) {
            LOGGER.error("inner client's password incorrect.");
            return Either.right(new FormatRespDto(Status.UNAUTHORIZED, ErrorRespDto.build(ErrorEnum.LOGIN_FAILED)));
        }

        LOGGER.info("build user info and generate access token.");
        String accessToken = generateAccessToken(buildClientUserInfoMap(inClientId));

        LOGGER.info("get access token succeed! userFlag = {}", userFlag);
        return Either.left(new GetAccessTokenRespDto(accessToken));
    }

    private Map<String, Object> buildClientUserInfoMap(String clientId) {
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("user_name", clientId);
        userInfoMap.put("userName", clientId);
        userInfoMap.put("userId", clientId);
        userInfoMap.put("authorities",
            Arrays.stream(EnumPlatform.values()).map(plat -> "ROLE_" + plat + "_" + EnumRole.TENANT.toString())
                .collect(Collectors.toList()));

        enhanceCommonInfo(userInfoMap);
        return userInfoMap;
    }

    private void enhanceCommonInfo(Map<String, Object> userInfoMap) {
        userInfoMap.put("jti", UUID.randomUUID().toString());
        userInfoMap.put("exp", System.currentTimeMillis() / 1000 + Consts.SECOND_HALF_DAY);
        userInfoMap.put("scope", Arrays.asList("all"));
    }

    private String generateAccessToken(Map<String, Object> userInfoMap) {
        String content = jsonParser.formatMap(userInfoMap);
        PrivateKey privateKey = keyPair.getPrivate();
        Signer signer = new RsaSigner((RSAPrivateKey) privateKey);
        return JwtHelper.encode(content, signer).getEncoded();
    }
}

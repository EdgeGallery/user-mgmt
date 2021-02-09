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

package org.edgegallery.user.auth.service;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.config.validate.annotation.ParameterValidate;
import org.edgegallery.user.auth.controller.dto.request.QueryUserReqDto;
import org.edgegallery.user.auth.controller.dto.request.RetrievePasswordReqDto;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.request.UniqueReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.QueryUserRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.controller.dto.response.UniquenessRespDto;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.db.custom.TenantTransactionRepository;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPermissionVo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("userMgmtService")
public class UserMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMgmtService.class);

    @Autowired
    private TenantPoMapper mapper;

    @Autowired
    private TenantTransactionRepository tenantTransaction;

    @Autowired
    private Pbkdf2PasswordEncoder passwordEncoder;

    @Autowired
    private SmsConfig smsConfig;

    /**
     * register user info by telephone, verification code and so on.
     *
     * @param reqParam reqParam
     * @return
     */
    @ParameterValidate
    public Either<TenantRespDto, FormatRespDto> register(TenantRegisterReqDto reqParam) {
        LOGGER.info("Begin register user");
        if (!StringUtils.isEmpty(reqParam.getTelephone()) && mapper.getTenantByTelephone(reqParam.getTelephone()) != null) {
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "Telephone has existed"));
        }

        if (!StringUtils.isEmpty(reqParam.getMailAddress()) && mapper.getTenantByMailAddress(reqParam.getMailAddress()) != null) {
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "MailAddress has existed"));
        }

        if (!StringUtils.isEmpty(reqParam.getUsername()) && mapper.getTenantByUsername(reqParam.getUsername()) != null) {
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "Username has existed"));
        }

        TenantRegisterReqDto registerRequest = reqParam;

        TenantPermissionVo tenantVo = new TenantPermissionVo();
        tenantVo.setTenantId(UUID.randomUUID().toString());
        tenantVo.setUsername(registerRequest.getUsername());
        tenantVo.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        tenantVo.setCompany(registerRequest.getCompany());
        tenantVo.setGender(registerRequest.getGender());
        if (!StringUtils.isEmpty(reqParam.getTelephone())) {
            tenantVo.setTelephoneNumber(registerRequest.getTelephone());
        }
        if (!StringUtils.isEmpty(reqParam.getMailAddress())) {
            tenantVo.setMailAddress(registerRequest.getMailAddress());
        }
        tenantVo.setAllowed(registerRequest.isAllowed());

        List<RolePo> rolePoList = new ArrayList<>();
        rolePoList.add(new RolePo(EnumPlatform.APPSTORE, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.DEVELOPER, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.MECM, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.ATP, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.LAB, EnumRole.TENANT));
        tenantVo.setRoles(rolePoList);

        int result = 0;
        try {
            result += tenantTransaction.registerTenant(tenantVo);
        } catch (Exception e) {
            LOGGER.error("Database Operate Exception: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Database Exception"));
        }

        if (result > 0) {
            TenantRespDto tenantRespDto = new TenantRespDto();
            tenantRespDto.setResponse(mapper.getTenantBasicPoData(tenantVo.getTenantId()));
            tenantRespDto.setPermission(mapper.getRolePoByTenantId(tenantVo.getTenantId()));
            LOGGER.info("User register success");
            return Either.left(tenantRespDto);
        } else {
            LOGGER.error("User register failed");
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "User register failed"));
        }
    }

    /**
     * whether the verification code is correct.
     *
     * @param verificationCode verificationCode
     * @param telephone telephone
     * @return
     */
    private boolean verifySmsCode(String verificationCode, String telephone) {
        if (!Boolean.parseBoolean(smsConfig.getEnabled())) {
            LOGGER.info("Sms is not enabled,no need to verify sms code");
            return true;
        }
        if (StringUtils.isEmpty(verificationCode) || !verificationCode
            .equals(RedisUtil.get(RedisUtil.RedisKeyType.verificationCode, telephone))) {
            return false;
        }
        RedisUtil.delete(RedisUtil.RedisKeyType.verificationCode, telephone);
        return true;
    }

    /**
     * forget and modify pw by telephone and verification code.
     *
     * @param retireveRequest retireveRequest
     * @return
     */
    @ParameterValidate
    public Either<Boolean, FormatRespDto> retrievePassword(RetrievePasswordReqDto retireveRequest) {
        LOGGER.info("Begin retrieve password");
        String telephone = retireveRequest.getTelephone();
        String verificationCode = retireveRequest.getVerificationCode();
        //username is not exit
        TenantPo tenantPo = mapper.getTenantByTelephone(telephone);

        if (tenantPo == null) {
            LOGGER.error("Telephone not exist");
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "Telephone not exist"));
        }

        if (!verifySmsCode(verificationCode, telephone)) {
            LOGGER.error("verification code is error ");
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "Verification code is error"));
        }

        int result = 0;
        try {
            result += mapper
                .modifyPassword(tenantPo.getTenantId(), passwordEncoder.encode(retireveRequest.getNewPassword()));
        } catch (Exception e) {
            LOGGER.error("Database Operate Exception: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Database Exception."));
        }

        if (result > 0) {
            LOGGER.info("Modify password success");
            return Either.left(true);
        } else {
            LOGGER.error("Modify password failed");
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "Modify password fail"));
        }
    }

    /**
     * Verify that the username and telephone number and mail address exist.
     *
     * @param uniqueRequest UniqueReqDto
     * @return
     */
    public Either<UniquenessRespDto, FormatRespDto> uniqueness(UniqueReqDto uniqueRequest) {
        String mailAddress = uniqueRequest.getMailAddress();
        String telephone = uniqueRequest.getTelephone();
        String username = uniqueRequest.getUsername();
        UniquenessRespDto uniquenessResponse = new UniquenessRespDto();

        if (mailAddress != null && mailAddress.length() > 1 && mapper.getTenantByMailAddress(mailAddress) != null) {
            uniquenessResponse.setMailAddress(true);
        }

        if (telephone != null && telephone.length() > 1 && mapper.getTenantByTelephone(telephone) != null) {
            uniquenessResponse.setTelephone(true);
        }

        if (username != null && username.length() > 1 && mapper.getTenantByUsername(username) != null) {
            uniquenessResponse.setUsername(true);
        }

        return Either.left(uniquenessResponse);
    }

    /**
     * delete user by id.
     *
     * @param tenantId user id
     */
    public boolean deleteUser(String tenantId) {
        tenantTransaction.deleteUser(tenantId);
        return true;
    }

    /**
     * query user from db.
     *
     * @param queryReq query request
     * @return TenantRespDto List
     */
    @ParameterValidate
    public Either<QueryUserRespDto, FormatRespDto> queryUsers(QueryUserReqDto queryReq) {
        try {
            QueryUserRespDto queryResp = new QueryUserRespDto();
            queryResp.setUserList(mapper.queryUsers(queryReq));
            queryResp.setTotalCount(mapper.queryUserCount(queryReq));
            return Either.left(queryResp);
        } catch (Exception e) {
            LOGGER.error("Database Exception on Query Users: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Database Exception"));
        }
    }

    /**
     * update user status
     *
     * @param tenantId user id
     * @param allowFlag allow flag
     * @return String or FormatRespDto
     */
    public Either<String, FormatRespDto> updateUserStatus(String tenantId, boolean allowFlag) {
        try {
            if (mapper.updateStatus(tenantId, allowFlag)) {
                return Either.left("");
            }
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "User not exist."));
        } catch (Exception e) {
            LOGGER.error("Database Exception on Update User Status: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Database Exception"));
        }
    }

    /**
     * modify the user info.
     *
     * @param user new user info
     * @return TenantRespDto
     */
    public Either<TenantRespDto, FormatRespDto> modifyUser(TenantRespDto user) {
        TenantPo oldUserPo = mapper.getTenantBasicPoData(user.getUserId());
        UniqueReqDto uniqueReqDto = new UniqueReqDto();
        if (!oldUserPo.getUsername().equals(user.getUsername())) {
            uniqueReqDto.setUsername(user.getUsername());
        }
        if (user.getTelephone() != null && !user.getTelephone().equals(oldUserPo.getTelephoneNumber())) {
            uniqueReqDto.setTelephone(user.getTelephone());
        }
        if (user.getMailAddress() != null && !user.getMailAddress().equals(oldUserPo.getMailAddress())) {
            uniqueReqDto.setMailAddress(user.getMailAddress());
        }
        String msg = "";
        Either<UniquenessRespDto, FormatRespDto> uniqueness = uniqueness(uniqueReqDto);
        if (uniqueness.isLeft()) {
            if (uniqueness.left().value().isMailAddress()) {
                msg = "repeat of mail address.";
            }
            if (uniqueness.left().value().isTelephone()) {
                msg = "repeat of telephone.";
            }
            if (uniqueness.left().value().isUsername()) {
                msg += "repeat of username.";
            }
            if (!msg.isEmpty()) {
                return Either.right(new FormatRespDto(Status.BAD_REQUEST, msg));
            }
        }
        tenantTransaction.updateTenant(user);
        TenantRespDto tenantRespDto = new TenantRespDto();
        tenantRespDto.setResponse(mapper.getTenantBasicPoData(user.getUserId()));
        return Either.left(tenantRespDto);
    }

    /**
     * modify the user settings.
     *
     * @param modifyReq modify request
     * @return String or FormatRespDto
     */
    public Either<String, FormatRespDto> modifyUserSetting(TenantRespDto modifyReq) {
        try {
            tenantTransaction.updateTenantSetting(modifyReq);
            return Either.left("");
        } catch (Exception e) {
            LOGGER.error("Database Exception on Modify User Settings: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR, "Database Exception"));
        }
    }
}

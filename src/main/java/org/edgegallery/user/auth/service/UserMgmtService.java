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

package org.edgegallery.user.auth.service;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.user.auth.config.validate.annotation.ParameterValidate;
import org.edgegallery.user.auth.controller.dto.request.ModifyPasswordReqDto;
import org.edgegallery.user.auth.controller.dto.request.QueryUserReqDto;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.request.UniqueReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.QueryUserRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantBasicRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.controller.dto.response.UniquenessRespDto;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.db.custom.TenantTransactionRepository;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPermissionVo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.utils.Consts;
import org.edgegallery.user.auth.utils.ErrorEnum;
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
    private IdentityService identityService;

    /**
     * register user info by telephone, verification code and so on.
     *
     * @param reqParam reqParam
     * @return
     */
    @ParameterValidate
    public Either<TenantRespDto, FormatRespDto> register(TenantRegisterReqDto reqParam) {
        LOGGER.info("Begin register user");
        if (!StringUtils.isEmpty(reqParam.getTelephone())
            && mapper.getTenantByTelephone(reqParam.getTelephone()) != null) {
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "Telephone has existed"));
        }

        if (!StringUtils.isEmpty(reqParam.getMailAddress())
            && mapper.getTenantByMailAddress(reqParam.getMailAddress()) != null) {
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "MailAddress has existed"));
        }

        if (!StringUtils.isEmpty(reqParam.getUsername())
            && mapper.getTenantByUsername(reqParam.getUsername()) != null) {
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "Username has existed"));
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
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                ErrorEnum.DATABASE_EXCEPTION.detail()));
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
     * modify password.
     *
     * @param modifyRequest modify request dto
     * @param currUserName current user name
     * @return modify result
     */
    @ParameterValidate
    public Either<Boolean, FormatRespDto> modifyPassword(ModifyPasswordReqDto modifyRequest, String currUserName) {
        LOGGER.info("Begin modify password");
        //User not exit
        TenantPo tenantPo = findTenantToModifyPw(modifyRequest, currUserName);
        if (tenantPo == null) {
            LOGGER.error(ErrorEnum.USER_NOT_FOUND.detail());
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorEnum.USER_NOT_FOUND.detail()));
        }

        Either<Boolean, FormatRespDto> checkResult = checkOnModifyPw(modifyRequest, tenantPo);
        if (checkResult != null) {
            return checkResult;
        }

        int result = 0;
        try {
            result += mapper
                .modifyPassword(tenantPo.getTenantId(), passwordEncoder.encode(modifyRequest.getNewPassword()));
        } catch (Exception e) {
            LOGGER.error("Database Operate Exception: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                ErrorEnum.DATABASE_EXCEPTION.detail()));
        }

        if (result > 0) {
            LOGGER.info("Modify password success");
            return Either.left(true);
        } else {
            LOGGER.error("Modify password failed");
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, "Modify password fail"));
        }
    }

    private Either<Boolean, FormatRespDto> checkOnModifyPw(ModifyPasswordReqDto modifyRequest,
        TenantPo tenantPo) {
        //check verify code
        if (modifyRequest.isRetrieveType()) {
            LOGGER.info("check verification code");
            String keyOfVerifyCode = StringUtils.isEmpty(modifyRequest.getTelephone())
                ? modifyRequest.getMailAddress()
                : modifyRequest.getTelephone();
            String verificationCode = modifyRequest.getVerificationCode();
            if (!identityService.checkVerificatinCode(RedisUtil.RedisKeyType.VERIFICATION_CODE,
                keyOfVerifyCode, verificationCode)) {
                LOGGER.error("verification code is error");
                return Either.right(new FormatRespDto(Status.BAD_REQUEST, "Verification code is error"));
            }
        } else {
            // check old pw
            LOGGER.info("check password");
            try {
                if (!passwordEncoder.matches(modifyRequest.getOldPassword(), tenantPo.getPassword())) {
                    LOGGER.error(ErrorEnum.PASSWORD_INCORRECT.detail());
                    return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorEnum.PASSWORD_INCORRECT.detail()));
                }
            } catch (Exception e) {
                LOGGER.error("failed for password match.");
                return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorEnum.PASSWORD_INCORRECT.detail()));
            }
        }
        return null;
    }

    private TenantPo findTenantToModifyPw(ModifyPasswordReqDto modifyRequest, String currUserName) {
        if (!modifyRequest.isRetrieveType()) {
            return mapper.getTenantByUsername(currUserName);
        } else {
            return StringUtils.isEmpty(modifyRequest.getTelephone())
                ? mapper.getTenantByMailAddress(modifyRequest.getMailAddress())
                : mapper.getTenantByTelephone(modifyRequest.getTelephone());
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
            queryResp.getUserList().forEach(TenantBasicRespDto::anonymize);
            return Either.left(queryResp);
        } catch (Exception e) {
            LOGGER.error("Database Exception on Query Users: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                ErrorEnum.DATABASE_EXCEPTION.detail()));
        }
    }

    /**
     * update user status.
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
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorEnum.USER_NOT_FOUND.detail()));
        } catch (Exception e) {
            LOGGER.error("Database Exception on Update User Status: {}", e.getMessage());
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                ErrorEnum.DATABASE_EXCEPTION.detail()));
        }
    }

    /**
     * modify the user info.
     *
     * @param user new user info
     * @param currUserName current user name
     * @return TenantRespDto
     */
    public Either<TenantRespDto, FormatRespDto> modifyUser(TenantRespDto user, String currUserName) {
        LOGGER.info("Begin modify user.");
        TenantPo oldUserPo = mapper.getTenantBasicPoData(user.getUserId());
        if (oldUserPo == null) {
            LOGGER.error(ErrorEnum.USER_NOT_FOUND.detail());
            return Either.right(new FormatRespDto(Status.BAD_REQUEST, ErrorEnum.USER_NOT_FOUND.detail()));
        }

        if (!oldUserPo.getUsername().equalsIgnoreCase(currUserName)
            && !Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(currUserName)) {
            LOGGER.error("The user has no permission to modify user.");
            return Either.right(new FormatRespDto(Status.FORBIDDEN, "The user has no permission to modify user."));
        }

        LOGGER.info("correct modify information.");
        user.assignAnonymizedValue(oldUserPo);

        LOGGER.info("check unique for user information.");
        Either<TenantRespDto, FormatRespDto> checkResult = checkUniqueOnModifyUser(user, oldUserPo);
        if (checkResult != null) {
            return checkResult;
        }

        LOGGER.info("save user information to database.");
        tenantTransaction.updateTenant(user);
        TenantRespDto tenantRespDto = new TenantRespDto();
        tenantRespDto.setResponse(mapper.getTenantBasicPoData(user.getUserId()));
        return Either.left(tenantRespDto);
    }

    private Either<TenantRespDto, FormatRespDto> checkUniqueOnModifyUser(TenantRespDto user,
        TenantPo oldUserPo) {
        UniqueReqDto uniqueReqDto = new UniqueReqDto();
        uniqueReqDto.setUsername(oldUserPo.getUsername().equals(user.getUsername()) ? null : user.getUsername());
        uniqueReqDto.setTelephone(
            user.getTelephone() == null || user.getTelephone().equals(oldUserPo.getTelephoneNumber())
                ? null : user.getTelephone());
        uniqueReqDto.setMailAddress(
            user.getMailAddress() == null || user.getMailAddress().equals(oldUserPo.getMailAddress())
                ? null : user.getMailAddress());
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
        return null;
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
            return Either.right(new FormatRespDto(Status.INTERNAL_SERVER_ERROR,
                ErrorEnum.DATABASE_EXCEPTION.detail()));
        }
    }
}

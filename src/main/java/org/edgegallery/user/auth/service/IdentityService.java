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
import java.security.SecureRandom;
import javax.ws.rs.core.Response;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.config.validate.annotation.ParameterValidate;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqByMailDto;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("identityService")
public class IdentityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    @Autowired
    private HwCloudVerification verification;

    @Autowired
    private MailService mailService;

    @Autowired
    private SmsConfig smsConfig;

    @Value("${mail.enabled}")
    private String mailEnabled;

    private String randomCode() {
        StringBuilder str = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    /**
     * send verification code by sms.
     * @param verifyRequest verify request
     * @return send result
     */
    @ParameterValidate
    public Either<Boolean, FormatRespDto> sendVerificationCodeBySms(VerificationReqDto verifyRequest) {
        if (!Boolean.parseBoolean(smsConfig.getEnabled())) {
            LOGGER.info("Sms is not enabled,no need to verify telephone param");
            return Either.left(true);
        }
        String telephone = verifyRequest.getTelephone();
        String verificationCode = randomCode();
        try {
            if (!verification.sendVerificationCode(telephone, verificationCode)) {
                LOGGER.error("send verification code by sms fail");
                return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                        "send verification code by sms fail, please again try."));
            }
        } catch (Exception e) {
            LOGGER.error("connection out");
            return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                    "connection out, please again try."));
        }

        RedisUtil.save(RedisUtil.RedisKeyType.verificationCode, telephone, verificationCode);
        LOGGER.info("send verification code by sms success");
        return Either.left(true);
    }

    /**
     * send verification code by mail.
     * @param verifyRequest verify request
     * @return send result
     */
    @ParameterValidate
    public Either<Boolean, FormatRespDto> sendVerificationCodeByMail(VerificationReqByMailDto verifyRequest) {
        if (!Boolean.parseBoolean(mailEnabled)) {
            LOGGER.info("Mail is not enabled,no need to verify mailAddress param");
            return Either.left(true);
        }
        String mailAddress = verifyRequest.getMailAddress();
        String verificationCode = randomCode();
        String subject = "Test";
        String content = verificationCode;
        if (!mailService.sendSimpleMail(mailAddress, subject, content)) {
            LOGGER.error("send verification code by mail fail");
            return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                "send verification code by mail fail"));
        }

        RedisUtil.save(RedisUtil.RedisKeyType.verificationCode, mailAddress, verificationCode);
        LOGGER.info("send verification code by mail success");
        return Either.left(true);
    }
}

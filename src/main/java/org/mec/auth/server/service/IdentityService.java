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

package org.mec.auth.server.service;

import fj.data.Either;
import java.security.SecureRandom;
import javax.ws.rs.core.Response;
import org.mec.auth.server.config.SmsConfig;
import org.mec.auth.server.config.validate.annotation.ParameterValidate;
import org.mec.auth.server.controller.dto.request.VerificationReqDto;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mec.auth.server.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("identityService")
public class IdentityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    @Autowired
    private HwCloudVerification verification;

    @Autowired
    private SmsConfig smsConfig;

    private String randomCode() {
        StringBuilder str = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    /**
     * send verification code.
     * @param telRequest telephone param
     * @return
     */
    @ParameterValidate
    public Either<Boolean, FormatRespDto> verifyTelParam(VerificationReqDto telRequest) {
        if (!Boolean.parseBoolean(smsConfig.getEnabled())) {
            LOGGER.info("Sms is not enabled,no need to verify telephone param");
            return Either.left(true);
        }
        String telephone = telRequest.getTelephone();
        String verificationCode = randomCode();
        try {
            if (!verification.sendVerificationCode(telephone, verificationCode)) {
                LOGGER.error("send verification fail");
                return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                        "send verification fail,please again try."));
            }
        } catch (Exception e) {
            LOGGER.error("connection out");
            return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                    "connection out,please again try."));
        }

        RedisUtil.saveVerificationCode(telephone, verificationCode);
        LOGGER.info("send verification code success");
        return Either.left(true);
    }
}

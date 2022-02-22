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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.user.auth.config.RedisConfig;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.config.validate.annotation.ParameterValidate;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqByMailDto;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestUtils;

@Service("identityService")
public class IdentityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private static final String MAIL_SUBJECT_VERIFYCODE = "[EdgeGallery] Please receive your verification code";

    private static final String MAIL_CONTENT_VERIFYCODE = "Hello!%n"
        + "The edgegallery platform is verifing your email, the verification code is: %s.%n"
        + "It will expire in %d minutes.";

    private static final int VERIFY_CODE_IMG_WIDTH = 175;

    private static final int VERIFY_CODE_IMG_HEIGHT = 60;

    private static final int VERIFY_CODE_IMG_FONTCOLOR = 255;

    private static final Random RANDOM_INSTANCE = new SecureRandom();

    @Autowired
    private TenantPoMapper mapper;

    @Autowired
    private HwCloudVerification verification;

    @Autowired
    private MailService mailService;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private RedisConfig redisConfig;

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
            LOGGER.info("Sms is not enabled,no need to verify telephone.");
            return Either.left(true);
        }

        String telephone = verifyRequest.getTelephone();
        if (mapper.getTenantByTelephone(telephone) == null) {
            LOGGER.error("telephone not exist,no need to verify telephone.");
            return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST,
                ErrorRespDto.build(ErrorEnum.MOBILEPHONE_NOT_FOUND)));
        }

        String smsVerificationCode = randomCode();
        try {
            if (!verification.sendVerificationCode(telephone, smsVerificationCode)) {
                LOGGER.error("send verification code by sms failed.");
                return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                        ErrorRespDto.build(ErrorEnum.SEND_VERIFYCODE_SMS_FAILED)));
            }
        } catch (Exception e) {
            LOGGER.error("sms server connect failed.");
            return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                    ErrorRespDto.build(ErrorEnum.SMS_CONNECT_FAILED)));
        }

        RedisUtil.save(RedisUtil.RedisKeyType.VERIFICATION_CODE, telephone, smsVerificationCode);
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
            LOGGER.info("Mail is not enabled,no need to verify mailAddress.");
            return Either.left(true);
        }

        String mailAddress = verifyRequest.getMailAddress();
        if (mapper.getTenantByMailAddress(mailAddress) == null) {
            LOGGER.info("mailAddress not exist,no need to verify mailAddress.");
            return Either.right(new FormatRespDto(Response.Status.BAD_REQUEST,
                ErrorRespDto.build(ErrorEnum.MAILADDR_NOT_FOUND)));
        }

        String mailVerificationCode = randomCode();
        String subject = MAIL_SUBJECT_VERIFYCODE;
        String content = String.format(MAIL_CONTENT_VERIFYCODE,
            mailVerificationCode, redisConfig.getVerificationTimeOut() / 60);
        if (!mailService.sendSimpleMail(mailAddress, subject, content)) {
            LOGGER.error("send verification code by mail failed.");
            return Either.right(new FormatRespDto(Response.Status.EXPECTATION_FAILED,
                ErrorRespDto.build(ErrorEnum.SEND_VERIFYCODE_MAIL_FAILED)));
        }

        RedisUtil.save(RedisUtil.RedisKeyType.VERIFICATION_CODE, mailAddress, mailVerificationCode);
        LOGGER.info("send verification code by mail success");
        return Either.left(true);
    }

    /**
     * generate image verification code.
     * @return image data
     */
    public BufferedImage generateImgVerificationCode() {
        String verifyCode = generateVerificationCode();
        BufferedImage bufferedImage = generateImageWithVerifyCode(verifyCode);

        String key = httpSession.getId();
        RedisUtil.save(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE, key, verifyCode);

        LOGGER.info("generate verification code image success");
        return bufferedImage;
    }

    private String generateVerificationCode() {
        StringBuilder randVerifyCode = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            randVerifyCode.append(RANDOM_INSTANCE.nextInt(10));
        }
        return randVerifyCode.toString();
    }

    private BufferedImage generateImageWithVerifyCode(String verifyCode) {
        BufferedImage bufferedImage = new BufferedImage(VERIFY_CODE_IMG_WIDTH, VERIFY_CODE_IMG_HEIGHT,
            BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        graphics.setColor(new Color(56, 33, 137));
        graphics.fillRect(0, 0, VERIFY_CODE_IMG_WIDTH, VERIFY_CODE_IMG_HEIGHT);

        graphics.setFont(new Font(null, Font.BOLD, 36));
        graphics.setColor(new Color(VERIFY_CODE_IMG_FONTCOLOR, VERIFY_CODE_IMG_FONTCOLOR, VERIFY_CODE_IMG_FONTCOLOR));
        for (int i = 0; i < verifyCode.length(); i++) {
            graphics.drawString(String.valueOf(verifyCode.charAt(i)), 30 * i + 35, VERIFY_CODE_IMG_HEIGHT - 15);
        }

        graphics.dispose();
        return bufferedImage;
    }

    /**
     * precheck image verification code.
     *
     * @param httpServletRequest HTTP Servlet Request
     * @return check result
     */
    public Either<Map<String, Boolean>, FormatRespDto> preCheckImgVerificationCode(
        HttpServletRequest httpServletRequest) {
        String key = httpSession.getId();
        String verificationCode = ServletRequestUtils.getStringParameter(httpServletRequest, "verifyCode", "");

        // delete verification code on redis if check failed
        Map<String, Boolean> resultMap = new HashMap<>();
        boolean checkResult = true;
        if (StringUtils.isEmpty(verificationCode) || !verificationCode
            .equals(RedisUtil.get(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE, key))) {
            RedisUtil.delete(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE, key);
            checkResult = false;
        }

        resultMap.put("checkResult", checkResult);
        return Either.left(resultMap);
    }

    /**
     * check the verification code is correct.
     *
     * @param keyType key type
     * @param keyOfVerifyCode key of verify code
     * @param verificationCode verification code
     * @return check result
     */
    public boolean checkVerificatinCode(RedisUtil.RedisKeyType keyType, String keyOfVerifyCode,
        String verificationCode) {
        String verificationCodeInRedis = RedisUtil.get(keyType, keyOfVerifyCode);

        // delete verification code in redis first whether the check result is right or not
        RedisUtil.delete(keyType, keyOfVerifyCode);

        return !StringUtils.isEmpty(verificationCode)
            && verificationCode.equals(verificationCodeInRedis);
    }
}

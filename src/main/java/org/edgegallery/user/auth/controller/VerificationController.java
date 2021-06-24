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

package org.edgegallery.user.auth.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.config.DescriptionConfig;
import org.edgegallery.user.auth.controller.base.BeGenericServlet;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqByMailDto;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RestSchema(schemaId = "identity")
@RequestMapping("/v1/identity")
@Controller
public class VerificationController extends BeGenericServlet {

    @Autowired
    private IdentityService identityService;

    /**
     * send verification code by sms.
     *
     * @param request Request Content Body
     * @return send result
     */
    @PostMapping(value = "/sms", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send verification code by sms", response = Object.class, notes =
            DescriptionConfig.VERIFICATION_SMS_MSG)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = "Expectation Failed",
                response = ErrorRespDto.class)})
    public ResponseEntity<Object> sendVerificationCodeBySms(
            @ApiParam(value = "verificationRequest", required = true) @RequestBody VerificationReqDto request) {
        return buildCreatedResponse(identityService.sendVerificationCodeBySms(request));
    }

    /**
     * send verification code by mail.
     *
     * @param request Request Content Body
     * @return send result
     */
    @PostMapping(value = "/mail", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "send verification code by mail", response = Object.class, notes =
        DescriptionConfig.VERIFICATION_MAIL_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = "Expectation Failed",
            response = ErrorRespDto.class)})
    public ResponseEntity<Object> sendVerificationCodeByMail(
        @ApiParam(value = "verificationRequest", required = true) @RequestBody VerificationReqByMailDto request) {
        return buildCreatedResponse(identityService.sendVerificationCodeByMail(request));
    }

    /**
     * generate image verification code.
     *
     * @return generate result
     */
    @GetMapping(value = "/verifycode-image", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    @ApiOperation(value = "generate image verification code", response = Object.class)
    public byte[] generateImgVerificationCode() throws IOException {
        BufferedImage image = identityService.generateImgVerificationCode();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * precheck image verification code.
     *
     * @param httpServletRequest HTTP Servlet Request
     * @return check result
     */
    @GetMapping(value = "/verifycode-image/precheck", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "check image verification code", response = Object.class)
    public ResponseEntity<Object> preCheckImgVerificationCode(HttpServletRequest httpServletRequest) {
        return buildResponse(identityService.preCheckImgVerificationCode(httpServletRequest));
    }
}

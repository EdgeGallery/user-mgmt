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

package org.edgegallery.user.auth.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.config.DescriptionConfig;
import org.edgegallery.user.auth.controller.base.BeGenericServlet;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqByMailDto;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.service.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "identity")
@RequestMapping("/v1/identity")
@Controller
public class VerificationController extends BeGenericServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private IdentityService identityService;

    /**
     * send verification code by sms.
     *
     * @param httpServletRequest HTTP Servlet Request
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
            HttpServletRequest httpServletRequest,
            @ApiParam(value = "verificationRequest", required = true) @RequestBody VerificationReqDto request) {
        LOGGER.info("remote info, addr={}, host={}, uri={}, url={}, user={}", httpServletRequest.getRemoteAddr(),
            httpServletRequest.getRemoteHost(), httpServletRequest.getRequestURI(), httpServletRequest.getRequestURL(),
            httpServletRequest.getRemoteUser(), httpServletRequest.getRemotePort());
        return buildCreatedResponse(identityService.sendVerificationCodeBySms(request));
    }

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
}

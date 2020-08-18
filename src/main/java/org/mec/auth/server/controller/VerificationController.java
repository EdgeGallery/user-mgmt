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

package org.mec.auth.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.mec.auth.server.config.DescriptionConfig;
import org.mec.auth.server.controller.base.BeGenericServlet;
import org.mec.auth.server.controller.dto.request.VerificationReqDto;
import org.mec.auth.server.controller.dto.response.ErrorRespDto;
import org.mec.auth.server.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestSchema(schemaId = "identity")
@RequestMapping("/v1/identity")
@Controller
public class VerificationController extends BeGenericServlet {

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/sms", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "verification", response = Object.class, notes =
            DescriptionConfig.VERIFICATION_MSG)
    @ApiResponses(value = {
            @ApiResponse(code = HttpStatus.SC_EXPECTATION_FAILED, message = "Expectation Failed",
                response = ErrorRespDto.class)})
    public ResponseEntity<Object> verification(
            @ApiParam(value = "verificationRequest", required = true) @RequestBody VerificationReqDto request) {
        return buildCreatedResponse(identityService.verifyTelParam(request));
    }
}

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

package org.edgegallery.user.auth.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.controller.base.AbstractBeGenericServlet;
import org.edgegallery.user.auth.controller.dto.request.GetAccessTokenReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.controller.dto.response.GetAccessTokenRespDto;
import org.edgegallery.user.auth.service.AccessTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RestSchema(schemaId = "access-token")
@RequestMapping("/v1/accesstoken")
@Controller
public class AccessTokenController extends AbstractBeGenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenController.class);

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * get access token.
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "get access token", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = "get access token success",
            response = GetAccessTokenRespDto.class),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request",
            response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = "get access token failed",
            response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> getAccessToken(@ApiParam(value = "GetAccessTokenReqDto", required = true) @RequestBody
        GetAccessTokenReqDto getAccessTokenReqDto) {
        LOGGER.info("get access token in controller.");
        return buildResponse(accessTokenService.getAccessToken(getAccessTokenReqDto));
    }
}

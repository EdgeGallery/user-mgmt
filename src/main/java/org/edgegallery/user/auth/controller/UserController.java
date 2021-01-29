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
import java.util.List;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.config.DescriptionConfig;
import org.edgegallery.user.auth.controller.base.BeGenericServlet;
import org.edgegallery.user.auth.controller.dto.request.QueryUserReqDto;
import org.edgegallery.user.auth.controller.dto.request.RetrievePasswordReqDto;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.request.UniqueReqDto;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.service.UserMgmtService;
import org.edgegallery.user.auth.utils.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "users-mgmt")
@RequestMapping("/v1/users")
@Controller
public class UserController extends BeGenericServlet {

    private static final String REG_UUID = "[0-9a-f]{32}";

    @Autowired
    private UserMgmtService userMgmtService;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "register", response = Object.class, notes = DescriptionConfig.REGISTER_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_CREATED, message = "register success", response = TenantRespDto.class),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "FORBIDDEN", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "INTERNAL ERROR",
            response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> register(
        @ApiParam(value = "TenantRegisterReqDto", required = true) @RequestBody TenantRegisterReqDto request) {
        return buildCreatedResponse(userMgmtService.register(request));
    }

    @PutMapping(value = "/password", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "forget password", response = Object.class, notes = DescriptionConfig.RETRIEVE_PASSWORD_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Forbidden", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "INTERNAL ERROR",
            response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> retrievePassword(
        @ApiParam(value = "RetrievePasswordReqDto", required = true) @RequestBody RetrievePasswordReqDto request) {
        return buildResponse(userMgmtService.retrievePassword(request));
    }

    @PostMapping(value = "/action/uniqueness", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "check uniqueness", response = Object.class, notes = DescriptionConfig.UNIQUENESS_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> uniqueness(
        @ApiParam(value = "uniquenessRequest", required = true) @RequestBody UniqueReqDto request) {
        return buildResponse(userMgmtService.uniqueness(request));
    }

    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "delete user by userId", response = Object.class)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class)
    })
    public ResponseEntity<String> deleteUser(
        @ApiParam(value = "user id") @PathVariable("userId") @Pattern(regexp = REG_UUID) String userId) {
        userMgmtService.deleteUser(userId);
        return ResponseEntity.ok("");
    }

    /**
     * query users
     */
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "query users.", response = Object.class)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = "query success", response = List.class),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "FORBIDDEN", response = ErrorRespDto.class),
        @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "INTERNAL ERROR", response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> queryUsers(
            @ApiParam(value = "QueryUserReqDto", required = true) @RequestBody QueryUserReqDto request) {
        // login user must be admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(authentication.getName())) {
            FormatRespDto formatRespDto = new FormatRespDto(Response.Status.FORBIDDEN,
                    "The user has no permission to query users.");
            return ResponseEntity.status(formatRespDto.getErrStatus().getStatusCode()).body(formatRespDto.getErrorRespDto());
        }

        // return users
        return buildResponse(userMgmtService.queryUsers(request));
    }

    @PutMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "modify user, not include password.", response = Object.class)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.SC_OK, message = "modify success", response = TenantRespDto.class),
        @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Bad Request", response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> modifyUser(
        @ApiParam(value = "user id") @PathVariable("userId") @Pattern(regexp = REG_UUID) String userId,
        @ApiParam(value = "TenantRegisterReqDto", required = true) @RequestBody TenantRespDto request) {
        request.setUserId(userId);
        return buildResponse(userMgmtService.modifyUser(request));
    }
}

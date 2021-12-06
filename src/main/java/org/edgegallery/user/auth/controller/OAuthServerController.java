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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.config.DescriptionConfig;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.db.EnumPlatform;
import org.edgegallery.user.auth.db.entity.RolePo;
import org.edgegallery.user.auth.db.entity.TenantPo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.edgegallery.user.auth.external.iam.ExternalUserUtil;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestSchema(schemaId = "auth")
@RequestMapping("/auth")
@Controller
public class OAuthServerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthServerController.class);

    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

    @Autowired
    private TenantPoMapper tenantPoMapper;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * logout.
     */
    @GetMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "logout", response = String.class, notes = DescriptionConfig.LOGOUT_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Internal error",
            response = ErrorRespDto.class)
    })
    public ResponseEntity<String> logout(HttpServletRequest request) {
        new SecurityContextLogoutHandler().logout(request, null, null);
        String ssoSessionId = request.getRequestedSessionId();
        oauthClientDetailsConfig.getEnabledClients().forEach(clientDetail -> {
            String clientUrl = clientDetail.getClientUrl();
            if (StringUtils.isEmpty(clientUrl)) {
                return;
            }
            String url = clientUrl + "/auth/logout?ssoSessionId=" + ssoSessionId;
            try {
                REST_TEMPLATE.getForObject(url, String.class);
            } catch (RestClientException e) {
                LOGGER.warn("can not access logout: {}", clientDetail.getClientId());
            }
        });
        return new ResponseEntity<>("Succeed", HttpStatus.OK);
    }

    /**
     * get current login user.
     */
    @GetMapping(value = "/login-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "get current login user.", response = Object.class)
    @ApiResponses(value = {
        @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Bad Request",
            response = ErrorRespDto.class)
    })
    public ResponseEntity<Object> getLoginUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getName() == null) {
            FormatRespDto response = new FormatRespDto(Response.Status.NOT_FOUND,
                ErrorRespDto.build(ErrorEnum.NO_LOGIN_USER));
            return ResponseEntity.status(response.getErrStatus().getStatusCode()).body(response.getErrorRespDto());
        }

        boolean isExternalUser = ExternalUserUtil.isExternalUser(authentication.getName());
        if (isExternalUser) {
            return buildExternalUserResp(authentication);
        } else {
            LOGGER.info(String.format("%s want to get login user information.", authentication.getName()));
            TenantPo user = tenantPoMapper.getTenantByUsername(authentication.getName());
            if (user == null) {
                FormatRespDto response = new FormatRespDto(Response.Status.NOT_FOUND,
                    ErrorRespDto.build(ErrorEnum.NO_LOGIN_USER));
                return ResponseEntity.status(response.getErrStatus().getStatusCode()).body(response.getErrorRespDto());
            }
            TenantRespDto tenantRespDto = new TenantRespDto();
            tenantRespDto.setResponse(user);
            tenantRespDto.setPermission(tenantPoMapper.getRolePoByTenantId(tenantRespDto.getUserId()));
            return ResponseEntity.ok(tenantRespDto);
        }
    }

    private ResponseEntity<Object> buildExternalUserResp(Authentication authentication) {
        ExternalUser externalUser = new ExternalUser();
        externalUser.parse(authentication.getName());
        LOGGER.info(String.format("%s want to get login user information.", externalUser.getUserName()));

        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(externalUser.getUserId());
        tenantPo.setUsername(externalUser.getUserName());
        tenantPo.setMailAddress(externalUser.getMailAddress());

        TenantRespDto tenantRespDto = new TenantRespDto();
        tenantRespDto.setResponse(tenantPo);

        List<RolePo> rolePos = Arrays.stream(EnumPlatform.values())
            .map(plat -> new RolePo(plat, ExternalUserUtil.convertUserRole(externalUser.getUserRole())))
            .collect(Collectors.toList());
        tenantRespDto.setPermission(rolePos);
        return ResponseEntity.ok(tenantRespDto);
    }
}

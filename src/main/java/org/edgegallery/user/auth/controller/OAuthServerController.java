/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.user.auth.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.user.auth.config.DescriptionConfig;
import org.edgegallery.user.auth.config.OAuthClientDetailsConfig;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestSchema(schemaId = "auth")
@RequestMapping("/auth")
@Controller
public class OAuthServerController {
    @Autowired
    private OAuthClientDetailsConfig oauthClientDetailsConfig;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * logout.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "logout", response = String.class, notes = DescriptionConfig.LOGOUT_MSG)
    @ApiResponses(value = {
        @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Internal error",
            response = ErrorRespDto.class)})
    public ResponseEntity<String> logout(HttpServletRequest request) {
        new SecurityContextLogoutHandler().logout(request, null, null);
        String ssoSessionId = request.getRequestedSessionId();
        oauthClientDetailsConfig.getClients().forEach(clientDetail -> {
            String clientUrl = clientDetail.getClientUrl();
            String url = clientUrl + "/auth/logout?ssoSessionId=" + ssoSessionId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "";
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            REST_TEMPLATE.postForObject(url, entity, String.class);
        });
        return new ResponseEntity<>("Succeed", HttpStatus.OK);
    }
}

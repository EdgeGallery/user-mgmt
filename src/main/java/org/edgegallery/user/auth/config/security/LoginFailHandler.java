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

package org.edgegallery.user.auth.config.security;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginFailHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFailHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorRespDto errorRespDto = ErrorRespDto.build(ErrorEnum.LOGIN_FAILED);
        errorRespDto.setMessage(exception.getLocalizedMessage());
        if (exception instanceof LockedException) {
            response.setStatus(HttpStatus.LOCKED.value());
            response.getWriter().println(
                new Gson().toJson(errorRespDto));
        } else {
            if (errorRespDto.getMessage().equalsIgnoreCase(ErrorEnum.VERIFY_CODE_ERROR.message())) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                errorRespDto.setCode(ErrorEnum.VERIFY_CODE_ERROR.code());
                response.getWriter().println(new Gson()
                    .toJson(errorRespDto));
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().println(new Gson()
                    .toJson(errorRespDto));
            }
        }
        LOGGER.error("failed to get token.");
    }
}

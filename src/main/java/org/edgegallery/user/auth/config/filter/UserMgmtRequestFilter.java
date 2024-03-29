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

package org.edgegallery.user.auth.config.filter;

import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.service.IdentityService;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

@Component
public class UserMgmtRequestFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMgmtRequestFilter.class);
    private static final String[] URL_PATTERNS = {"/login", "/logout", "/auth/", "/v1/"};
    private static final String[] CHECK_VERIFY_CODE_URL_PATTERNS = {"/v1/identity/sms", "/v1/identity/sms/",
        "/v1/identity/mail", "/v1/identity/mail/",
        "/v1/users", "/v1/users/"};
    private static final String UNKNOWN = "unknown";

    @Autowired
    private IdentityService identityService;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (!checkVerificationCode(request, response)) {
            LOGGER.error("invalid verification code, forbbiden access.");
            return;
        }

        String url = request.getRequestURI();
        if (!StringUtils.startsWithAny(url, URL_PATTERNS)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        HttpRequestLog logs = new HttpRequestLog();
        try {
            logs.setRequest(logForRequest(request));
            filterChain.doFilter(request, response);
        } finally {
            logs.setResponse(logForResponse(response));
            updateResponse(response);
            LOGGER.info("Http Request log: {}", new Gson().toJson(logs));
        }
    }

    private boolean checkVerificationCode(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String url = request.getRequestURI();
        if (!StringUtils.equalsAny(url, CHECK_VERIFY_CODE_URL_PATTERNS)) {
            return true;
        }

        String verificationCode = ServletRequestUtils.getStringParameter(request, "verifyCode", "");
        if (!identityService.checkVerificatinCode(RedisUtil.RedisKeyType.IMG_VERIFICATION_CODE,
            request.getSession().getId(), verificationCode)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().println(new Gson()
                .toJson(ErrorRespDto.build(ErrorEnum.VERIFY_CODE_ERROR)));
            return false;
        }

        return true;
    }

    private HttpRequestTraceLog logForRequest(HttpServletRequest request) {
        HttpRequestTraceLog requestTraceLog = new HttpRequestTraceLog();
        requestTraceLog.setTime(LocalDateTime.now().toString());
        requestTraceLog.setPath(request.getRequestURI());
        requestTraceLog.setMethod(request.getMethod());
        requestTraceLog.setIp(getIpAddress(request));
        return requestTraceLog;
    }

    private HttpResponseTraceLog logForResponse(HttpServletResponse response) {
        HttpResponseTraceLog responseTraceLog = new HttpResponseTraceLog();
        responseTraceLog.setStatus(response.getStatus());
        responseTraceLog.setTime(LocalDateTime.now().toString());
        return responseTraceLog;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // the first IP is the real IP
        if (!StringUtils.isEmpty(ip)) {
            return ip.split(",")[0];
        } else {
            return UNKNOWN;
        }
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils
            .getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }

    @Setter
    @Getter
    private static class HttpRequestLog {
        HttpRequestTraceLog request;

        HttpResponseTraceLog response;
    }

    @Setter
    @Getter
    private static class HttpRequestTraceLog {
        private String path;

        private String userId;

        private String method;

        private String time;

        private String requestBody;

        private String ip;
    }

    @Setter
    @Getter
    private static class HttpResponseTraceLog {
        private Integer status;

        private String time;

        private String body;
    }
}

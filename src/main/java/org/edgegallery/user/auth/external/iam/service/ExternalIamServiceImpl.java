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

package org.edgegallery.user.auth.external.iam.service;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.edgegallery.user.auth.external.iam.IExternalIamService;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalIamServiceImpl implements IExternalIamService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalIamServiceImpl.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Value("${external.iam.endpoint}")
    private String externalIamUrl;

    @Override
    public ExternalUser login(String userFlag, String password) {
        LOGGER.debug("external login.");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("userFlag", userFlag);
        loginReq.put("password", password);

        String url = externalIamUrl + "/iam/users/login";
        HttpEntity<String> requestEntity = new HttpEntity<>(new Gson().toJson(loginReq), headers);
        try {
            ResponseEntity<ExternalUser> response = REST_TEMPLATE
                .exchange(url, HttpMethod.POST, requestEntity, ExternalUser.class);
            if (response == null || response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("external login failed.");
                return null;
            }

            LOGGER.debug("external login succeed.");
            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("external login failed, exception {}", e.getMessage());
            return null;
        }
    }
}

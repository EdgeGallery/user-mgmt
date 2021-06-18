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

import com.google.gson.Gson;
import fj.data.Either;
import javax.ws.rs.core.Response;
import org.edgegallery.user.auth.controller.dto.response.ErrorRespDto;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.service.IdentityService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class VerficationApiTest {

    private MockMvc mvc;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    protected VerificationController verificationController;

    private Gson gson = new Gson();

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(verificationController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_successfully_when_verify_tel_true() throws Exception {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("15194251243");

        Either<Boolean, FormatRespDto> response = Either.left(true);
        Mockito.when(identityService.sendVerificationCodeBySms(Mockito.any(VerificationReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/identity/sms").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request)).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void should_fail_when_verify_tel_bad_request() throws Exception {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("15194251243");

        Either<Boolean, FormatRespDto> response = Either
            .right(new FormatRespDto(Response.Status.BAD_REQUEST,
                ErrorRespDto.build(ErrorEnum.SEND_VERIFYCODE_SMS_FAILED)));
        Mockito.when(identityService.sendVerificationCodeBySms(Mockito.any(VerificationReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/identity/sms").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request)).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

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

import fj.data.Either;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(MockitoJUnitRunner.class)
public class RegisterApiTest extends UserControllerTest {

    @Test
    public void registerSuccessful() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");

        Either<TenantRespDto, FormatRespDto> response = Either.left(new TenantRespDto());
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isCreated());

    }

    @Test
    public void registerFailUserLength() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("user");
        request.setPassword("password");

        Either<TenantRespDto, FormatRespDto> response = Either.right(new FormatRespDto(Status.BAD_REQUEST, "Data format error."));
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    public void registerFailPasswordLength() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("pass");

        Either<TenantRespDto, FormatRespDto> response = Either.right(new FormatRespDto(Status.BAD_REQUEST, "Data format error."));
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    public void registerFailDB() throws Exception {

        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        Either<TenantRespDto, FormatRespDto> response = Either.right(new FormatRespDto(Status.EXPECTATION_FAILED, "Save user information exception."));
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isExpectationFailed());
    }

    @Test
    public void registerFailUsernameRequire() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setPassword("password");
        Either<TenantRespDto, FormatRespDto> response = Either.right(new FormatRespDto(Status.BAD_REQUEST, "Data Required error."));
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void registerFailPasswordRequire() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        Either<TenantRespDto, FormatRespDto> response = Either.right(new FormatRespDto(Status.BAD_REQUEST, "Data Required error."));
        Mockito.when(userMgmtService.register(Mockito.any(TenantRegisterReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(request))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}

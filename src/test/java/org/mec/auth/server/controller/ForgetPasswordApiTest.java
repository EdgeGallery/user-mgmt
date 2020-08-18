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

import fj.data.Either;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mec.auth.server.controller.dto.request.RetrievePasswordReqDto;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ForgetPasswordApiTest extends UserControllerTest {
    @Test
    public void testForgetPasswordSuccess() throws Exception {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setVerificationCode("123456");
        request.setNewPassword("password");
        request.setTelephone("15194251243");

        Either<Boolean, FormatRespDto> response = Either.left(true);
        Mockito.when(userMgmtService.retrievePassword(Mockito.any(RetrievePasswordReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.put("/v1/users/password").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(gson.toJson(request)).accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testForgetPasswordFail1() throws Exception {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setVerificationCode("123456");
        request.setNewPassword("password");
        request.setTelephone("15194251243");

        Either<Boolean, FormatRespDto> response = Either.right(new FormatRespDto(Response.Status.FORBIDDEN, "Forbidden or No Permission to Access."));
        Mockito.when(userMgmtService.retrievePassword(Mockito.any(RetrievePasswordReqDto.class))).thenReturn(response);

        mvc.perform(MockMvcRequestBuilders.put("/v1/users/password").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(gson.toJson(request)).accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}

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

package org.mec.auth.server.service;

import fj.data.Either;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;
import org.mec.auth.server.controller.dto.request.RetrievePasswordReqDto;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mec.auth.server.db.entity.TenantPo;
import org.mockito.Mockito;

public class RetrievePasswordTest extends UserMgmtTest {

    @Test
    public void retrievePasswordSuccess() {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setTelephone("15191881234");
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };

        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("password");

        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(UUID.randomUUID().toString());
        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(tenantPo);
        Mockito.when(mapper.modifyPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        Either<Boolean, FormatRespDto> either = userMgmtService.retrievePassword(request);
        Assert.assertTrue(either.isLeft());
    }

    /**
     * verification code is wrong
     */
    @Test
    public void retrievePasswordFail1() {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setTelephone("15191881234");
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return false;
            }
        };
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("password");
        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(UUID.randomUUID().toString());
        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(tenantPo);
        Mockito.when(mapper.modifyPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        Either<Boolean, FormatRespDto> either = userMgmtService.retrievePassword(request);
        Assert.assertTrue(either.isRight());
    }

    /**
     * db is wrong
     */
    @Test
    public void retrievePasswordFail2() {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setTelephone("15191881234");
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("password");
        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(UUID.randomUUID().toString());
        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(tenantPo);
        Mockito.when(mapper.modifyPassword(Mockito.anyString(), Mockito.anyString())).
                thenThrow(new RuntimeException("mapper exception"));
        Either<Boolean, FormatRespDto> either = userMgmtService.retrievePassword(request);
        Assert.assertTrue(either.isRight());
    }

    /**
     * telephone is not exit
     */
    @Test
    public void retrievePasswordFail3() {
        RetrievePasswordReqDto request = new RetrievePasswordReqDto();
        request.setTelephone("15191881234");
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };
        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("password");
        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(UUID.randomUUID().toString());
        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.modifyPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(1);
        Either<Boolean, FormatRespDto> either = userMgmtService.retrievePassword(request);
        Assert.assertTrue(either.isRight());
    }



}

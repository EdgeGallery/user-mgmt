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

package org.edgegallery.user.auth.service;

import fj.data.Either;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.controller.dto.request.ModifyPasswordReqDto;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RetrievePasswordTest {
    @Autowired
    protected UserMgmtService userMgmtService;

    @Autowired
    protected TenantPoMapper mapper;

    private String tenantId;

    private String telephone = "15191881234";

    @Before
    public void registerUser() {
        String username = "s" + RandomStringUtils.randomAlphanumeric(16);
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername(username);
        request.setPassword("password1234.");
        request.setTelephone(telephone);
        request.setCompany("huawei");
        request.setGender("male");
        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isLeft());
        tenantId = either.left().value().getUserId();
    }

    @After
    public void deleteUser() {
        if (tenantId != null) {
            mapper.deleteUser(tenantId);
        }
    }

    @Test
    public void should_successfully_when_modify_password() {
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifyCode(String verificationCode, String keyOfVerifyCode) {
                return true;
            }
        };

        ModifyPasswordReqDto retrieveRequest = new ModifyPasswordReqDto();
        retrieveRequest.setTelephone(telephone);
        retrieveRequest.setNewPassword("newPassword1234.");
        retrieveRequest.setVerificationCode("123456");
        Either<Boolean, FormatRespDto> retrieveEither = userMgmtService.modifyPassword(retrieveRequest, "");
        Assert.assertTrue(retrieveEither.isLeft());
    }

    @Test
    public void should_failed_when_wrong_verify_code() {
        ModifyPasswordReqDto request = new ModifyPasswordReqDto();
        request.setTelephone(telephone);
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifyCode(String verificationCode, String keyOfVerifyCode) {
                return false;
            }
        };
        Either<Boolean, FormatRespDto> either = userMgmtService.modifyPassword(request, "");
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifyCode(String verificationCode, String keyOfVerifyCode) {
                return true;
            }
        };
        Assert.assertTrue(either.isRight());
    }

    @Test
    public void should_failed_when_telephone_not_exit() {
        ModifyPasswordReqDto request = new ModifyPasswordReqDto();
        request.setTelephone("15945678912");
        request.setNewPassword("newPassword1234.");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifyCode(String verificationCode, String keyOfVerifyCode) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = userMgmtService.modifyPassword(request, "");
        Assert.assertTrue(either.isRight());
    }

    @Test
    public void should_failed_when_password_less_6(){
        ModifyPasswordReqDto request = new ModifyPasswordReqDto();
        request.setTelephone(telephone);
        request.setNewPassword("ne9%$");
        request.setVerificationCode("123456");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifyCode(String verificationCode, String keyOfVerifyCode) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = userMgmtService.modifyPassword(request, "");
        Assert.assertTrue(either.isRight());
    }
}

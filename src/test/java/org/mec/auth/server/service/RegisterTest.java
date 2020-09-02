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
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.auth.server.MainServer;
import org.mec.auth.server.controller.dto.request.TenantRegisterReqDto;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mec.auth.server.controller.dto.response.TenantRespDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RegisterTest {

    @Autowired
    protected UserMgmtService userMgmtService;

    @Test
    public void should_successfully_when_register_new_user() {
        String username = "s" + RandomStringUtils.randomAlphanumeric(16);
        String telephone = "13" + RandomStringUtils.randomNumeric(9);

        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername(username);
        request.setPassword("password1234.");
        request.setTelephone(telephone);
        request.setCompany("huawei");
        request.setGender("male");
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isLeft());
    }

    // username length fail
    @Test
    public void should_failed_when_username_less_6() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("user5");
        request.setPassword("nihao!@123456");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(400, either.right().value().getErrStatus().getStatusCode());

    }

    // password length fail
    @Test
    public void should_failed_when_pw_less_6() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("!@1Ab");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // no username
    @Test
    public void should_failed_when_no_username() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setPassword("nihao!@123456");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // no password
    @Test
    public void should_failed_when_no_password() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // telephone has existed
    @Test
    public void should_failed_when_telNo_existed() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("nihao!@123456");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // username has existed
    @Test
    public void should_failed_when_username_existed() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("nihao!@123456");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isLeft());
        request.setTelephone("15012345677");
        either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // verification code is error
    @Test
    public void should_failed_when_sms_code_error() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("nihao!@123456");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return false;
            }
        };

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // // something error
    // @Test
    // public void registerFail10() {
    //     TenantRegisterReqDto request = new TenantRegisterReqDto();
    //     request.setUsername("username");
    //     request.setPassword("nihao!@123456");
    //     request.setTelephone("15012345678");
    //     request.setCompany("huawei");
    //     request.setGender("male");
    //     new MockUp<UserMgmtService>() {
    //         @Mock
    //         private boolean verifySmsCode(String verificationCode, String telephone) {
    //             return true;
    //         }
    //     };
    //
    //     Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
    //     Assert.assertTrue(either.isRight());
    //     System.out.println(either.right().value());
    // }

}

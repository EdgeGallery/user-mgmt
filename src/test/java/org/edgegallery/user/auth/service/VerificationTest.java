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
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.ws.rs.core.Response;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.config.SmsConfig;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqByMailDto;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.utils.HttpsUtil;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.ServletRequestUtils;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class VerificationTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private SmsConfig smsConfig;

    @Before
    public void begin() {
        smsConfig.setEnabled("true");
    }

    @After
    public void end() {
        smsConfig.setEnabled("false");
    }

    @Test
    public void sms_should_successfully_when_normal() {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000003");
        new MockUp<HttpsUtil>() {
            @Mock
            public boolean httpsPost(String url, Map<String, String> headers, String bodyParam) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isLeft());
        Assert.assertTrue(either.left().value());
    }

    @Test
    public void sms_should_failed_when_http_failed() {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000003");
        new MockUp<HttpsUtil>() {
            @Mock
            public boolean httpsPost(String url, Map<String, String> headers, String bodyParam) {
                return false;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(either.right().value().getErrStatus(), Response.Status.EXPECTATION_FAILED);
    }

    @Test
    public void sms_should_successfully_when_disabled() {
        smsConfig.setEnabled("false");
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000003");
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isLeft());
        Assert.assertTrue(either.left().value());
    }

    @Test
    public void sms_should_failed_when_no_telephone() {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000099");
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(either.right().value().getErrStatus(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void mail_should_successfully_when_disabled() {
        VerificationReqByMailDto request = new VerificationReqByMailDto();
        request.setMailAddress("13800000003@edgegallery.org");
        MockUp booleanMockup = new MockUp<Boolean>() {
            @Mock
            public boolean parseBoolean(String boolValue) {
                return false;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeByMail(request);
        booleanMockup.tearDown();

        Assert.assertTrue(either.isLeft());
        Assert.assertTrue(either.left().value());
    }

    @Test
    public void mail_should_successfully_when_normal() {
        VerificationReqByMailDto request = new VerificationReqByMailDto();
        request.setMailAddress("13800000003@edgegallery.org");
        new MockUp<Boolean>() {
            @Mock
            public boolean parseBoolean(String boolValue) {
                return true;
            }
        };
        MockUp mailServiceMockup = new MockUp<MailService>() {
            @Mock
            public boolean sendSimpleMail(String receiver, String subject, String content) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeByMail(request);
        mailServiceMockup.tearDown();

        Assert.assertTrue(either.isLeft());
        Assert.assertTrue(either.left().value());
    }

    @Test
    public void mail_should_failed_when_no_mailaddress() {
        VerificationReqByMailDto request = new VerificationReqByMailDto();
        request.setMailAddress("13800000099@edgegallery.org");
        new MockUp<Boolean>() {
            @Mock
            public boolean parseBoolean(String boolValue) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeByMail(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(either.right().value().getErrStatus(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void mail_should_failed_when_send_failed() {
        VerificationReqByMailDto request = new VerificationReqByMailDto();
        request.setMailAddress("13800000003@edgegallery.org");
        new MockUp<Boolean>() {
            @Mock
            public boolean parseBoolean(String boolValue) {
                return true;
            }
        };
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeByMail(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(either.right().value().getErrStatus(), Response.Status.EXPECTATION_FAILED);
    }

    @Test
    public void generate_img_code() {
        BufferedImage bi= identityService.generateImgVerificationCode();
        Assert.assertNotNull(bi);
    }

    @Test
    public void pre_check_img_code_right() {
        new MockUp<ServletRequestUtils>() {
            @Mock
            public String getStringParameter(ServletRequest request, String name, String defaultVal) {
                return "1234";
            }
        };
        new MockUp<RedisUtil>() {
            @Mock
            public String get(RedisUtil.RedisKeyType type, String key) {
                return "1234";
            }
        };
        Either<Map<String, Boolean>, FormatRespDto> eitherResult = identityService.preCheckImgVerificationCode(null);
        Assert.assertTrue(eitherResult.isLeft());
        Map<String, Boolean> checkResult = eitherResult.left().value();
        Assert.assertNotNull(checkResult);
        Assert.assertTrue(checkResult.containsKey("checkResult"));
        Assert.assertTrue(checkResult.get("checkResult"));
    }

    @Test
    public void pre_check_img_code_wrong() {
        new MockUp<ServletRequestUtils>() {
            @Mock
            public String getStringParameter(ServletRequest request, String name, String defaultVal) {
                return "1234";
            }
        };
        new MockUp<RedisUtil>() {
            @Mock
            public String get(RedisUtil.RedisKeyType type, String key) {
                return "4321";
            }
        };
        Either<Map<String, Boolean>, FormatRespDto> eitherResult = identityService.preCheckImgVerificationCode(null);
        Assert.assertTrue(eitherResult.isLeft());
        Map<String, Boolean> checkResult = eitherResult.left().value();
        Assert.assertNotNull(checkResult);
        Assert.assertTrue(checkResult.containsKey("checkResult"));
        Assert.assertFalse(checkResult.get("checkResult"));
    }
}

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
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.config.SmsConfig;
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

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class VerificationTest {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HwCloudVerification hwCloudVerification;

    @Autowired
    private SmsConfig smsConfig;

    @Autowired
    private HttpsUtil httpsUtil;

    @Before
    public void begin() {
        smsConfig.setEnabled("true");
    }

    @After
    public void end() {
        smsConfig.setEnabled("false");
    }

    @Test
    public void should_successfully_when_right_verify_code() {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000003");
        new MockUp<RedisUtil>() {
            @Mock
            public void save(RedisUtil.RedisKeyType type, String key, String value) {
                return;
            }
        };
        new MockUp<HttpsUtil>() {
            @Mock
            public boolean httpsPost(String url, Map<String, String> headers, String bodyParam) {
                return true;
            }
        };
        Map<String, String> anyMap = new HashMap<>();
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void should_failed_when_wrong_verify_code() {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("13800000003");
        new MockUp<RedisUtil>() {
            @Mock
            public void save(RedisUtil.RedisKeyType type, String key, String value) {
                return;
            }
        };
        new MockUp<HttpsUtil>() {
            @Mock
            public boolean httpsPost(String url, Map<String, String> headers, String bodyParam) {
                return false;
            }
        };
        Map<String, String> anyMap = new HashMap<>();
        Either<Boolean, FormatRespDto> either = identityService.sendVerificationCodeBySms(request);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(either.right().value().getErrStatus(), Response.Status.EXPECTATION_FAILED);
    }
}

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

package org.edgegallery.user.auth.service;

import fj.data.Either;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.controller.dto.request.VerificationReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.utils.redis.RedisUtil;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class VerificationTest {

    @Autowired
    private IdentityService identityService;

    @MockBean
    private HwCloudVerification hwCloudVerification;

    @Test
    public void should_successfully_when_right_verify_code() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("15191881235");
        new MockUp<RedisUtil>() {
            @Mock
            public void saveVerificationCode(String key, String value) {
                return;
            }
        };
        Mockito.when(hwCloudVerification.sendVerificationCode(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
        Either<Boolean, FormatRespDto> either = identityService.verifyTelParam(request);
        Assert.assertTrue(either.isLeft());
    }

    @Test
    public void should_failed_when_wrong_verify_code() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        VerificationReqDto request = new VerificationReqDto();
        request.setTelephone("15191881235");
        new MockUp<RedisUtil>() {
            @Mock
            public void saveVerificationCode(String key, String value) {
                return;
            }
        };
        Mockito.when(hwCloudVerification.sendVerificationCode(Mockito.anyString(),Mockito.anyString())).thenReturn(false);
        Either<Boolean, FormatRespDto> either = identityService.verifyTelParam(request);
        Assert.assertTrue(either.isLeft());
    }
}

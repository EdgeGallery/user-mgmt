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
import java.util.Date;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.controller.dto.request.GetAccessTokenReqDto;
import org.edgegallery.user.auth.controller.dto.response.FormatRespDto;
import org.edgegallery.user.auth.controller.dto.response.GetAccessTokenRespDto;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.external.iam.IExternalIamService;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.edgegallery.user.auth.utils.ErrorEnum;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AccessTokenTest {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private IExternalIamService externalIamService;

    @Before
    public void begin() {
    }

    @After
    public void end() {
    }

    @Test
    public void get_accesstoken_invalidpara() {
        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.PARA_ILLEGAL.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_usernotfound_defaultiam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("tenantx");
        getAccessTokenReqDto.setPassword("tenantx");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_userlocked_defaultiam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("tenanty");
        getAccessTokenReqDto.setPassword("tenanty");
        int count = 0;
        while (count < 5) {
            Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService
                .getAccessToken(getAccessTokenReqDto);
            Assert.assertTrue(either.isRight());
            Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
            count++;
        }

        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.USER_LOCKED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_wrongpw_defaultiam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("tenant1");
        getAccessTokenReqDto.setPassword("tenantx");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_success_defaultiam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("tenant1");
        getAccessTokenReqDto.setPassword("tenant");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isLeft());
        Assert.assertNotNull(either.left().value().getAccessToken());
    }

    @Test
    public void get_accesstoken_failed_externaliam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", true);
        ReflectionTestUtils.setField(externalIamService, "externalIamUrl", "http://127.0.0.1");

        new MockUp<RestTemplate>() {
            @Mock
            public ResponseEntity<ExternalUser> exchange(String url, HttpMethod method,
                @Nullable HttpEntity<String> requestEntity, Class<ExternalUser> responseType, Object... uriVariables)
                throws RestClientException {
                return new ResponseEntity<ExternalUser>(new ExternalUser(), HttpStatus.UNAUTHORIZED);
            }
        };

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("username-test");
        getAccessTokenReqDto.setPassword("username-test-pw");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_success_externaliam() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", true);
        ReflectionTestUtils.setField(externalIamService, "externalIamUrl", "http://127.0.0.1");

        new MockUp<RestTemplate>() {
            @Mock
            public ResponseEntity<ExternalUser> exchange(String url, HttpMethod method,
                @Nullable HttpEntity<String> requestEntity, Class<ExternalUser> responseType, Object... uriVariables)
                throws RestClientException {
                return new ResponseEntity<ExternalUser>(
                    new ExternalUser("userid-test", "username-test", "", EnumRole.TENANT.name()), HttpStatus.OK);
            }
        };

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("username-test");
        getAccessTokenReqDto.setPassword("username-test-pw");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isLeft());
        Assert.assertNotNull(either.left().value().getAccessToken());
    }

    @Test
    public void get_accesstoken_userillegal_innerclient() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("test:");
        getAccessTokenReqDto.setPassword("test");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.PARA_ILLEGAL.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_timeout_innerclient() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("test:" + (new Date().getTime() - 6000));
        getAccessTokenReqDto.setPassword("test");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_wrongpw_innerclient() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("test:" + new Date().getTime());
        getAccessTokenReqDto.setPassword("test_wrongpw");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isRight());
        Assert.assertEquals(ErrorEnum.LOGIN_FAILED.code(), either.right().value().getErrorRespDto().getCode());
    }

    @Test
    public void get_accesstoken_success_innerclient() {
        ReflectionTestUtils.setField(accessTokenService, "externalIamEnabled", false);

        GetAccessTokenReqDto getAccessTokenReqDto = new GetAccessTokenReqDto();
        getAccessTokenReqDto.setUserFlag("test:" + new Date().getTime());
        getAccessTokenReqDto.setPassword("test");
        Either<GetAccessTokenRespDto, FormatRespDto> either = accessTokenService.getAccessToken(getAccessTokenReqDto);
        Assert.assertTrue(either.isLeft());
        Assert.assertNotNull(either.left().value().getAccessToken());
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mockit.Mock;
import mockit.MockUp;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mec.auth.server.controller.dto.request.TenantRegisterReqDto;
import org.mec.auth.server.controller.dto.response.FormatRespDto;
import org.mec.auth.server.controller.dto.response.TenantRespDto;
import org.mec.auth.server.db.EnumPlatform;
import org.mec.auth.server.db.EnumRole;
import org.mec.auth.server.db.entity.RolePo;
import org.mec.auth.server.db.entity.TenantPermissionVo;
import org.mec.auth.server.db.entity.TenantPo;
import org.mockito.Mockito;

public class RegisterTest extends UserMgmtTest{

    @Test
    public void registerSuccessful() {
        String username = "s" + RandomStringUtils.randomAlphanumeric(16);
        String telephone = "13" + RandomStringUtils.random(9);

        Mockito.when(tenantTransaction.registerTenant(Mockito.any(TenantPermissionVo.class))).thenReturn(5);

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.getTenantByUsername(Mockito.anyString())).thenReturn(null);

        TenantPo tenantPo = new TenantPo();
        tenantPo.setTenantId(UUID.randomUUID().toString());
        tenantPo.setUsername(username);
        tenantPo.setCompany("huawei");
        tenantPo.setTelephoneNumber(telephone);
        tenantPo.setGender("male");
        Mockito.when(mapper.getTenantBasicPoData(Mockito.anyString())).thenReturn(tenantPo);

        List<RolePo> rolePoList = new ArrayList<>();
        rolePoList.add(new RolePo(EnumPlatform.APPSTORE, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.DEVELOPER, EnumRole.TENANT));
        rolePoList.add(new RolePo(EnumPlatform.MECM, EnumRole.TENANT));

        Mockito.when(mapper.getRolePoByTenantId(Mockito.anyString())).thenReturn(rolePoList);
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername(username);
        request.setPassword("password1234.");
        request.setTelephone("15712345678");
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
    public void registerFail1() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("user");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // password length fail
    @Test
    public void registerFail2() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("pass");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // no username
    @Test
    public void registerFail4() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // no password
    @Test
    public void registerFail5() {
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
    public void registerFail6() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(new TenantPo());
        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // username has existed
    @Test
    public void registerFail7() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.getTenantByUsername(Mockito.anyString())).thenReturn(new TenantPo());
        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // verification code is error
    @Test
    public void registerFail8() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.getTenantByUsername(Mockito.anyString())).thenReturn(null);
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return false;
            }
        };

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

    // db exception
    @Test
    public void registerFail9() {

        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.getTenantByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.addTenantPo(Mockito.any(TenantPo.class))).thenThrow(new RuntimeException("mapper exception"));
        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());

    }

    // something error
    @Test
    public void registerFail10() {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username");
        request.setPassword("password");
        request.setTelephone("15012345678");
        request.setCompany("huawei");
        request.setGender("male");
        new MockUp<UserMgmtService>() {
            @Mock
            private boolean verifySmsCode(String verificationCode, String telephone) {
                return true;
            }
        };

        Mockito.when(mapper.getTenantByTelephone(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.getTenantByUsername(Mockito.anyString())).thenReturn(null);
        Mockito.when(mapper.addTenantPo(Mockito.any(TenantPo.class))).thenReturn(0);
        Mockito.when(mapper.insertPermission(Mockito.anyString(), Mockito.anyList())).thenReturn(0);

        Either<TenantRespDto, FormatRespDto> either = userMgmtService.register(request);
        Assert.assertTrue(either.isRight());
    }

}

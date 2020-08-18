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

package org.mec.auth.server;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.auth.server.db.entity.TenantPo;
import org.mec.auth.server.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DBTest {

    @Autowired
    private TenantPoMapper mapper;

    @Test
    public void testAddTetant() {
        TenantPo po = addTenantPo();
        Assert.assertNotNull(po);
    }

    @Test
    public void testGetTenantBasicPoData() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantBasicPoData(tenantPoAdd.getTenantId());
        Assert.assertEquals(tenantPoAdd.getUsername(), tenantPoGet.getUsername());
    }

    @Test
    public void testGetTenantByTelephone() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantByTelephone(tenantPoAdd.getTelephoneNumber());
        Assert.assertEquals(tenantPoAdd.getUsername(), tenantPoGet.getUsername());
    }

    @Test
    public void testGetTenantByUsername() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantByUsername(tenantPoAdd.getUsername());
        Assert.assertEquals(tenantPoAdd.getTenantId(), tenantPoGet.getTenantId());
    }

    private TenantPo addTenantPo() {
        TenantPo po = new TenantPo();
        po.setTenantId(UUID.randomUUID().toString());
        po.setUsername(RandomStringUtils.randomAlphanumeric(16));
        po.setPassword("password");
        po.setTelephoneNumber(RandomStringUtils.randomAlphanumeric(11));
        po.setGender("male");
        po.setCompany("huawei");

        int res = mapper.addTenantPo(po);
        return res == 1 ? po : null;
    }

}

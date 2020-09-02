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

import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.auth.server.db.entity.TenantPo;
import org.mec.auth.server.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest(classes = {MainServer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class DBTest {

    @Autowired
    private TenantPoMapper mapper;

    @Test
    public void should_successfully_when_add_tenant() {
        TenantPo po = addTenantPo();
        Assert.assertNotNull(po);
    }

    @Test
    public void should_successfully_when_get_tenant_by_tenantId() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantBasicPoData(tenantPoAdd.getTenantId());
        Assert.assertEquals(tenantPoAdd.getUsername(), tenantPoGet.getUsername());
    }

    @Test
    public void should_successfully_when_get_tenant_by_telephone() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantByTelephone(tenantPoAdd.getTelephoneNumber());
        Assert.assertEquals(tenantPoAdd.getUsername(), tenantPoGet.getUsername());
    }

    @Test
    public void should_successfully_when_get_tenant_by_name() {
        TenantPo tenantPoAdd = addTenantPo();
        TenantPo tenantPoGet = mapper.getTenantByUsername(tenantPoAdd.getUsername());
        Assert.assertEquals(tenantPoAdd.getTenantId(), tenantPoGet.getTenantId());
    }

    private TenantPo addTenantPo() {
        TenantPo po = new TenantPo();
        po.setTenantId(UUID.randomUUID().toString());
        po.setUsername(RandomStringUtils.randomAlphanumeric(16));
        po.setPassword("pw12#$");
        po.setTelephoneNumber("13"+ RandomStringUtils.randomNumeric(9));
        po.setGender("male");
        po.setCompany("huawei");

        int res = mapper.addTenantPo(po);
        return res == 1 ? po : null;
    }

}

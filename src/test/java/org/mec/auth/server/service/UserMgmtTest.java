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

import javax.sql.DataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.auth.server.db.custom.TenantTransactionRepository;
import org.mec.auth.server.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.filter.CorsFilter;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UserMgmtTest {

    @Autowired
    protected UserMgmtService userMgmtService;

    @MockBean
    private CorsFilter corsFilter;

    @MockBean
    protected TenantPoMapper mapper;

    @MockBean
    protected DataSource dataSource;

    @MockBean
    protected Pbkdf2PasswordEncoder passwordEncoder;

    @MockBean
    protected TenantTransactionRepository tenantTransaction;

    @Test
    public void init() {
        // empty method to avoid no runnable method exception
    }

}

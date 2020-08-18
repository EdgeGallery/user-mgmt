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

package org.mec.auth.server.controller;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mec.auth.server.service.UserMgmtService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CorsFilter;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    protected MockMvc mvc;

    @InjectMocks
    protected UserController userController;

    @MockBean
    private CorsFilter corsFilter;

    @Mock
    protected UserMgmtService userMgmtService;

    protected Gson gson = new Gson();

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(userController).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initTest() {
        // empty method to avoid no runnable method exception
    }

}

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

package org.edgegallery.user.auth.controller;

import com.google.gson.Gson;
import org.edgegallery.user.auth.service.UserMgmtService;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class UserControllerTest {

    protected MockMvc mvc;

    @InjectMocks
    protected UserController userController;

    @Mock
    protected UserMgmtService userMgmtService;

    protected Gson gson = new Gson();

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.standaloneSetup(userController).build();
        MockitoAnnotations.initMocks(this);
    }

}

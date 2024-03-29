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

package org.edgegallery.user.auth.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


import com.google.gson.Gson;
import java.util.Date;
import javax.servlet.http.Cookie;
import mockit.Mock;
import mockit.MockUp;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.config.security.MecUserDetailsServiceImpl;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.edgegallery.user.auth.db.EnumRole;
import org.edgegallery.user.auth.external.iam.IExternalIamService;
import org.edgegallery.user.auth.external.iam.model.ExternalUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainServer.class)
@AutoConfigureMockMvc
@Transactional
public class LoginTest {
    @Autowired
    private MockMvc mvc;

    protected Gson gson = new Gson();

    @Autowired
    private MecUserDetailsServiceImpl mecUserDetailsService;

    @Autowired
    private IExternalIamService externalIamService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private String xsrfToken;

    private Cookie[] cookies;

    @Before
    public void setUp() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).build();
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/try_to_get_xsrf_token").contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        this.xsrfToken = response.getCookie("XSRF-TOKEN").getValue();
        this.cookies = response.getCookies();

        new MockUp<MecUserDetailsServiceImpl>() {
            @Mock
            public void checkVerificationCode(String uniqueUserFlag) {
            }
        };
    }

    @Test
    public void should_successful_when_access_root_url() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void should_successful_when_login_with_admin() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "admin").param("password", "admin"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void should_successful_when_login_with_client_user() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "test:" + new Date().getTime()).param("password", "test"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void should_failed_when_login_with_client_user_timeout() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "test:" + (new Date().getTime() - 6000)).param("password", "test"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void should_failed_when_login_with_client_user_illegal() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "test:").param("password", "test"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }


    @Test
    public void should_failed_when_login_with_wrong_password() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "admin").param("password", "wrong-password"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void should_locked_when_login_error_over_5_times() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        int i = 0;
        while (i < 5) {
            mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
                .param("username", "tenant1").param("password", "wrong-password"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            i++;
        }
        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "tenant1").param("password", "tenant"))
            .andExpect(MockMvcResultMatchers.status().isLocked());
    }

    @Test
    public void should_successful_when_login_error_4_time() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        int i = 0;
        while (i < 4) {
            mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
                .param("username", "tenant2").param("password", "wrong-password"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            i++;
        }
        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "tenant2").param("password", "tenant")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void should_failed_when_login_with_no_username() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", false);

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "not_found_user").param("password", "Test!123"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void should_failed_when_login_externaliam() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", true);
        ReflectionTestUtils.setField(externalIamService, "externalIamUrl", "http://127.0.0.1");

        new MockUp<RestTemplate>() {
            @Mock
            public ResponseEntity<ExternalUser> exchange(String url, HttpMethod method,
                @Nullable HttpEntity<String> requestEntity, Class<ExternalUser> responseType, Object... uriVariables)
                throws RestClientException {
                throw new RestClientException("test failed.");
            }
        };

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "username-test").param("password", "username-test-pw"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void should_success_when_login_externaliam() throws Exception {
        ReflectionTestUtils.setField(mecUserDetailsService, "externalIamEnabled", true);
        ReflectionTestUtils.setField(externalIamService, "externalIamUrl", "http://127.0.0.1");

        new MockUp<RestTemplate>() {
            @Mock
            public ResponseEntity<ExternalUser> exchange(String url, HttpMethod method,
                @Nullable HttpEntity<String> requestEntity, Class<ExternalUser> responseType, Object... uriVariables)
                throws RestClientException {
                return new ResponseEntity<>(
                    new ExternalUser("userid-test", "username-test", "user-test@s.c", EnumRole.TENANT.name()),
                    HttpStatus.OK);
            }
        };

        mvc.perform(MockMvcRequestBuilders.post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE)
            .param("username", "username-test").param("password", "username-test-pw"))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "guest",
        roles = {"APPSTORE_GUEST", "DEVELOPER_GUEST", "MECM_GUEST", "LAB_GUEST", "ATP_GUEST"})
    public void should_return_guest_when_login_with_guest() throws Exception {
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/auth/login-info").contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        TenantRespDto user = gson.fromJson(content, TenantRespDto.class);
        assertFalse(user == null);
        assertEquals("guest", user.getUsername());
    }

    @Test
    public void should_failed_when_not_login_user() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/auth/login-info").contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "EU-1;userid-test;username-test;usermail-test;TENANT",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_externaluser() throws Exception {
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/auth/login-info").contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        TenantRespDto user = gson.fromJson(content, TenantRespDto.class);
        assertFalse(user == null);
        assertEquals("username-test", user.getUsername());
    }
}

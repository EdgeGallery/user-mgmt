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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.servlet.http.Cookie;
import org.edgegallery.user.auth.MainServer;
import org.edgegallery.user.auth.controller.dto.request.QueryUserCtrlDto;
import org.edgegallery.user.auth.controller.dto.request.QueryUserReqDto;
import org.edgegallery.user.auth.controller.dto.request.TenantRegisterReqDto;
import org.edgegallery.user.auth.controller.dto.response.QueryUserRespDto;
import org.edgegallery.user.auth.controller.dto.response.TenantRespDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainServer.class)
@AutoConfigureMockMvc
@Transactional
public class UserMgmtApiTest {

    @Autowired
    private MockMvc mvc;

    protected Gson gson = new Gson();

    @Autowired
    private WebApplicationContext webApplicationContext;

    private String xsrfToken;

    private Cookie[] cookies;

    private TenantRespDto registerUser() throws Exception {
        TenantRegisterReqDto request = new TenantRegisterReqDto();
        request.setUsername("username_test");
        request.setPassword("pw-test123");
        request.setTelephone("15822224444");
        request.setCompany("test");
        request.setGender("1");

        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.post("/v1/users").contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(request)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isCreated());
        MvcResult mvcResult = result.andReturn();
        String content = mvcResult.getResponse().getContentAsString(UTF_8);
        TenantRespDto user = gson.fromJson(content, TenantRespDto.class);
        return user;
    }

    @Before
    public void setUp() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).build();

        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/try_to_get_xsrf_token").contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        this.xsrfToken = response.getCookie("XSRF-TOKEN").getValue();
        this.cookies = response.getCookies();
    }

    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    @Test
    public void should_return_200_when_query_users_by_admin() throws Exception {
        QueryUserReqDto request = new QueryUserReqDto();
        request.setStatus(-1);
        QueryUserCtrlDto queryCtrl = new QueryUserCtrlDto();
        queryCtrl.setOffset(-1);
        queryCtrl.setLimit(0);
        request.setQueryCtrl(queryCtrl);
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/v1/users/list").contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(request)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        String content = mvcResult.getResponse().getContentAsString();
        QueryUserRespDto users = gson.fromJson(content, new TypeToken<QueryUserRespDto>() { }.getType());
        assertFalse(users.getUserList().isEmpty());
        assertEquals(users.getTotalCount(), users.getUserList().size());
    }

    @Test
    @WithMockUser(username = "guest",
        roles = {"APPSTORE_GUEST", "DEVELOPER_GUEST", "MECM_GUEST", "LAB_GUEST", "ATP_GUEST"})
    public void should_return_403_when_query_users_by_guest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/v1/users/list").contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tenant",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_403_when_query_users_by_tenant() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/v1/users/list").contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_200_when_delete_user_by_admin() throws Exception {
        String userId = "123456qw123456qw123456qw123456qw";
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.delete("/v1/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "guest",
        roles = {"APPSTORE_GUEST", "DEVELOPER_GUEST", "MECM_GUEST", "LAB_GUEST", "ATP_GUEST"})
    public void should_return_403_when_delete_user_by_guest() throws Exception {
        String userId = "123456qw123456qw123456qw123456qw";
        ResultActions result = mvc.perform(
            MockMvcRequestBuilders.delete("/v1/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).cookie(cookies))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "guest",
        roles = {"APPSTORE_GUEST", "DEVELOPER_GUEST", "MECM_GUEST", "LAB_GUEST", "ATP_GUEST"})
    public void should_return_403_when_modify_user_by_guest() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        tenant.setUsername("newName123");
        tenant.setUserId(null);
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tenant",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_403_when_modify_user_by_tenant() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        tenant.setUsername("newName123");
        tenant.setUserId(null);
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_200_when_modify_user_by_admin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        tenant.setUsername("newName123");
        tenant.setUserId(null);

        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.put("/v1/users/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        String content = mvcResult.getResponse().getContentAsString();
        TenantRespDto modifyUser = gson.fromJson(content, TenantRespDto.class);
        assertEquals("newName123", modifyUser.getUsername());
    }

    @Test
    @WithMockUser(username = "tenant",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_403_when_modify_usersetting_by_tenant() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(
            MockMvcRequestBuilders.put("/v1/users/settings/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAdminUser",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_403_when_modify_usersetting_by_nonsuperadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(
            MockMvcRequestBuilders.put("/v1/users/settings/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_ok_when_modify_usersetting_by_superadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(
            MockMvcRequestBuilders.put("/v1/users/settings/" + userId).contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-XSRF-TOKEN", xsrfToken).content(gson.toJson(tenant)).cookie(cookies)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "tenant",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_403_when_disallow_user_by_tenant() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/disallow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAdminUser",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_403_when_disallow_user_by_nonsuperadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/disallow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_failed_when_disallow_user_by_noexistuser() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = "noexistuser";
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/disallow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_ok_when_disallow_user_by_superadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/disallow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "tenant",
        roles = {"APPSTORE_TENANT", "DEVELOPER_TENANT", "MECM_TENANT", "LAB_TENANT", "ATP_TENANT"})
    public void should_return_403_when_allow_user_by_tenant() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/allow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testAdminUser",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_403_when_allow_user_by_nonsuperadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/allow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_failed_when_allow_user_by_noexistuser() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = "noexistuser";
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/allow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin",
        roles = {"APPSTORE_ADMIN", "DEVELOPER_ADMIN", "MECM_ADMIN", "LAB_ADMIN", "ATP_ADMIN"})
    public void should_return_ok_when_allow_user_by_superadmin() throws Exception {
        TenantRespDto tenant = registerUser();
        String userId = tenant.getUserId();
        mvc.perform(MockMvcRequestBuilders.put("/v1/users/status/" + userId + "/allow")
            .contentType(MediaType.APPLICATION_JSON_VALUE).header("X-XSRF-TOKEN", xsrfToken)
            .content(gson.toJson(tenant)).cookie(cookies).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

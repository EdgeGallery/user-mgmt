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

package org.mec.auth.server.controller.dto.response;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mec.auth.server.db.entity.RolePo;
import org.mec.auth.server.db.entity.TenantPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ToString
@Setter
@Getter
public class TenantRespDto extends TenantBasicRespDto {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantRespDto.class);

    @ApiModelProperty(required = true, example = "37423702-051a-46b4-bf2b-f190759cc0b8")
    private String userId;

    @ApiModelProperty(required = true)
    private List<RoleDto> permissions;

    /**
     * set tenant response value.
     */
    public void setResponse(TenantPo tenantPo) {
        this.setUserId(tenantPo.getTenantId());
        this.setUsername(tenantPo.getUsername());
        this.setCompany(tenantPo.getCompany());
        this.setGender(tenantPo.getGender());
        this.setTelephone(tenantPo.getTelephoneNumber());
    }

    /**
     * set tenant role permission.
     */
    public void setPermission(List<RolePo> rolePos) {
        List<RoleDto> roleDtos = new ArrayList<>();
        for (RolePo rolePo : rolePos) {
            RoleDto dto = new RoleDto();
            dto.setPlatform(rolePo.getPlatform());
            dto.setRole(rolePo.getRole());
            roleDtos.add(dto);
        }
        this.setPermissions(roleDtos);
    }

}

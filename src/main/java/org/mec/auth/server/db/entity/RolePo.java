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

package org.mec.auth.server.db.entity;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.mec.auth.server.db.EnumPlatform;
import org.mec.auth.server.db.EnumRole;

@Entity
@Table(name = "tbl_role")
@Setter
@Getter
@AllArgsConstructor
public class RolePo {

    private int id;

    @ApiModelProperty(required = true)
    private EnumPlatform platform;

    @ApiModelProperty(required = true)
    private EnumRole role;

    public RolePo(int id) {
        this.id = id;
    }

    /**
     *set role info.
     */
    public RolePo(EnumPlatform platform, EnumRole role) {
        this.platform = platform;
        this.role = role;
    }

    public String toString() {
        return platform + "_" + role;
    }

}

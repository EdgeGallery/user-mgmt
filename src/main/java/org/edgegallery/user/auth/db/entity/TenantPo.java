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

package org.edgegallery.user.auth.db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tbl_tenant")
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class TenantPo {

    @Id
    private String tenantId;

    private String username;

    private String password;

    private String company;

    private String telephoneNumber;

    private String mailAddress;

    private String gender;

    // default is true
    private boolean isAllowed = true;

    private String createTime;

    private String modifyTime;

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * generate tenant id.
     *
     * @return
     */
    public String getTenantId() {
        if (this.tenantId == null) {
            this.tenantId = UUID.randomUUID().toString();
        }
        return this.tenantId;
    }
}

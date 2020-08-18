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

package org.mec.auth.server.db.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.mec.auth.server.db.entity.RolePo;
import org.mec.auth.server.db.entity.TenantPo;

@Mapper
public interface TenantPoMapper {

    /**
     * saving the driver instance object to the DB using the mybaties.
     */

    TenantPo getTenantBasicPoData(String tenantId);

    TenantPo getTenantByTelephone(String telephoneNumber);

    TenantPo getTenantByUsername(String username);

    List<RolePo> getRolePoByTenantId(String tenantId);

    int addTenantPo(TenantPo tenantPo);

    int modifyPassword(String tenantId, String password);

    int insertPermission(String tenantId, List<RolePo> roles);

}

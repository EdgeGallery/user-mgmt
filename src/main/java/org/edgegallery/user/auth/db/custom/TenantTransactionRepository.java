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

package org.edgegallery.user.auth.db.custom;

import org.edgegallery.user.auth.db.entity.TenantPermissionVo;
import org.edgegallery.user.auth.db.mapper.TenantPoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(rollbackFor = {Exception.class})
public class TenantTransactionRepository {

    @Autowired
    private TenantPoMapper tenantPoMapper;


    /**
     * insert tenant into db using transactions.
     *
     * @param tenantVo tenantVo
     * @return
     */
    public int registerTenant(TenantPermissionVo tenantVo) {
        int result = 0;
        result += tenantPoMapper.addTenantPo(tenantVo);
        result += tenantPoMapper.insertPermission(tenantVo.getTenantId(), tenantVo.getRoles());
        return result;
    }

}
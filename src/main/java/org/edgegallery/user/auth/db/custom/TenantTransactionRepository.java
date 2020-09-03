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

-- supported platforms and roles
    insert into tbl_role (ID, PLATFORM, ROLE) values
    (1, 'APPSTORE', 'GUEST'),(2, 'APPSTORE', 'TENANT'),(3, 'APPSTORE', 'ADMIN'),
    (4, 'DEVELOPER', 'GUEST'),(5, 'DEVELOPER', 'TENANT'),(6, 'DEVELOPER', 'ADMIN'),
    (7, 'MECM', 'GUEST'),(8, 'MECM', 'TENANT'),(9, 'MECM', 'ADMIN')
    ON CONFLICT(id) do nothing;

-- add a guest user
    insert into tbl_tenant (TENANTID, USERNAME, PASSWORD, COMPANY, TELEPHONENUMBER, GENDER)
    values('de3565b1-a7c2-42b9-b281-3f032af29ff7', 'guest',
    '59756fda85ebddee6024d5cc0e6edcde3226693257a9c1eb662c56426b8a4f232b3d56c321adbd91', 'company', '13800000000', '1')
    ON CONFLICT(TENANTID) do nothing;

-- set the permissions for guest user
    insert into tbl_tenant_role (TENANTID, ROLEID) values
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 1),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 4),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 7)
    ON CONFLICT(TENANTID, ROLEID) do nothing;
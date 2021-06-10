-- supported platforms and roles
    insert into tbl_role (ID, PLATFORM, ROLE) values
    (1, 'APPSTORE', 'GUEST'),(2, 'APPSTORE', 'TENANT'),(3, 'APPSTORE', 'ADMIN'),
    (4, 'DEVELOPER', 'GUEST'),(5, 'DEVELOPER', 'TENANT'),(6, 'DEVELOPER', 'ADMIN'),
    (7, 'MECM', 'GUEST'),(8, 'MECM', 'TENANT'),(9, 'MECM', 'ADMIN'),
    (10, 'ATP', 'GUEST'),(11, 'ATP', 'TENANT'),(12, 'ATP', 'ADMIN'),
    (13, 'LAB', 'GUEST'),(14, 'LAB', 'TENANT'),(15, 'LAB', 'ADMIN')
    ON CONFLICT(id) do nothing;

-- add a guest user
    insert into tbl_tenant (TENANTID, USERNAME, PASSWORD, COMPANY, TELEPHONENUMBER, MAILADDRESS, GENDER, isallowed, CREATETIME, MODIFYTIME)
    values('de3565b1-a7c2-42b9-b281-3f032af29ff7', 'guest',
    '59756fda85ebddee6024d5cc0e6edcde3226693257a9c1eb662c56426b8a4f232b3d56c321adbd91', 'company', '13800000000', '13800000000@edgegallery.org', '1', true, now(), now())
    ON CONFLICT(TENANTID) do nothing;

-- add a admin user
    insert into tbl_tenant (TENANTID, USERNAME, PASSWORD, COMPANY, TELEPHONENUMBER, MAILADDRESS, GENDER, isallowed, CREATETIME, MODIFYTIME, PWEFFECTTIME)
    values('39937079-99fe-4cd8-881f-04ca8c4fe09d', 'admin',
    '45709693f38464c0c0fbf525ec7a740ddf63e28ef39e56836601dc43b00ed20c86713bbcd73bb215', 'company', '13800000001', '13800000001@edgegallery.org', '1', true, now(), now(), now() - interval '10y')
    ON CONFLICT(TENANTID) do nothing;

-- set the permissions for guest user
    insert into tbl_tenant_role (TENANTID, ROLEID) values
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 1),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 4),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 7),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 10),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 13)
    ON CONFLICT(TENANTID, ROLEID) do nothing;

    insert into tbl_tenant_role (TENANTID, ROLEID) values
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 3),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 6),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 9),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 12),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 15)
    ON CONFLICT(TENANTID, ROLEID) do nothing;
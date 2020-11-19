
--MERGE INTO tbl_role
--  KEY(ID)
--VALUES (1, 'APPSTORE', 'GUEST'),
--  (2, 'APPSTORE', 'TENANT'),
--  (3, 'APPSTORE', 'ADMIN'),
--  (4, 'DEVELOPER', 'GUEST'),
--  (5, 'DEVELOPER', 'TENANT'),
--  (6, 'DEVELOPER', 'ADMIN'),
--  (7, 'MECM', 'GUEST'),
--  (8, 'MECM', 'TENANT'),
--  (9, 'MECM', 'ADMIN');

-- supported platforms and roles
    MERGE INTO tbl_role (ID, PLATFORM, ROLE) key(id)  values
    (1, 'APPSTORE', 'GUEST'),(2, 'APPSTORE', 'TENANT'),(3, 'APPSTORE', 'ADMIN'),
    (4, 'DEVELOPER', 'GUEST'),(5, 'DEVELOPER', 'TENANT'),(6, 'DEVELOPER', 'ADMIN'),
    (7, 'MECM', 'GUEST'),(8, 'MECM', 'TENANT'),(9, 'MECM', 'ADMIN'),
    (10, 'ATP', 'GUEST'),(11, 'ATP', 'TENANT'),(12, 'ATP', 'ADMIN'),
    (13, 'LAB', 'GUEST'),(14, 'LAB', 'TENANT'),(15, 'LAB', 'ADMIN');
--    ON CONFLICT(id) do nothing;

-- add a guest user
    merge into tbl_tenant (TENANTID, USERNAME, PASSWORD, COMPANY, TELEPHONENUMBER, GENDER, isallowed) key(TENANTID)
    values('de3565b1-a7c2-42b9-b281-3f032af29ff7', 'guest',
    '59756fda85ebddee6024d5cc0e6edcde3226693257a9c1eb662c56426b8a4f232b3d56c321adbd91', 'company', '13800000000', '1', true);
--    ON CONFLICT(TENANTID) do nothing;

-- add a admin user
    merge into tbl_tenant (TENANTID, USERNAME, PASSWORD, COMPANY, TELEPHONENUMBER, GENDER, isallowed) key(TENANTID)
    values('39937079-99fe-4cd8-881f-04ca8c4fe09d', 'admin',
    '472645ad1af0101adaa6769cc865fec3b29fedeba6dc912900a59b1364b7a6bb17bb9a0575854547', 'company', '13800000001', '1', true);
--    ON CONFLICT(TENANTID) do nothing;

-- set the permissions for guest user
    merge into tbl_tenant_role (TENANTID, ROLEID) key(TENANTID, ROLEID)  values
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 1),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 4),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 7),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 10),
    ('de3565b1-a7c2-42b9-b281-3f032af29ff7', 13);
--    ON CONFLICT(TENANTID, ROLEID) do nothing;

    merge into tbl_tenant_role (TENANTID, ROLEID) key(TENANTID, ROLEID) values
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 3),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 6),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 9),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 12),
    ('39937079-99fe-4cd8-881f-04ca8c4fe09d', 15);
--    ON CONFLICT(TENANTID, ROLEID) do nothing;
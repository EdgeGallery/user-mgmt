
MERGE INTO tbl_role
  KEY(ID)
VALUES (1, 'APPSTORE', 'GUEST'),
  (2, 'APPSTORE', 'TENANT'),
  (3, 'APPSTORE', 'ADMIN'),
  (4, 'DEVELOPER', 'GUEST'),
  (5, 'DEVELOPER', 'TENANT'),
  (6, 'DEVELOPER', 'ADMIN'),
  (7, 'MECM', 'GUEST'),
  (8, 'MECM', 'TENANT'),
  (9, 'MECM', 'ADMIN');
--
--    insert into tbl_role (ID, PLATFORM, ROLE) values(1, 'APPSTORE', 'GUEST');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(2, 'APPSTORE', 'TENANT');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(3, 'APPSTORE', 'ADMIN');
--
--    insert into tbl_role (ID, PLATFORM, ROLE) values(4, 'DEVELOPER', 'GUEST');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(5, 'DEVELOPER', 'TENANT');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(6, 'DEVELOPER', 'ADMIN');
--
--    insert into tbl_role (ID, PLATFORM, ROLE) values(7, 'MECM', 'GUEST');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(8, 'MECM', 'TENANT');
--    insert into tbl_role (ID, PLATFORM, ROLE) values(9, 'MECM', 'ADMIN');
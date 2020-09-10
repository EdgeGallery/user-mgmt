
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

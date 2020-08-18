
CREATE TABLE if not exists tbl_tenant (
  	TENANTID            VARCHAR(100)      PRIMARY KEY NOT NULL,
  	USERNAME            VARCHAR(50)       NOT NULL,
  	PASSWORD            VARCHAR(100)      NOT NULL,
  	COMPANY             VARCHAR(50)       NULL,
  	TELEPHONENUMBER     VARCHAR(20)       NOT NULL,
  	GENDER              VARCHAR(10)       NULL,
  	CONSTRAINT USERNAME UNIQUE (username),
  	CONSTRAINT TELEPHONENUMBER UNIQUE (telephonenumber)

);

CREATE TABLE if not exists tbl_tenant_role (
 	  TENANTID            VARCHAR(100)      NOT NULL,
  	ROLEID              INTEGER           NOT NULL
);


    CREATE TABLE if not exists tbl_role (
        ID                   INTEGER                PRIMARY KEY NOT NULL,
    	  PLATFORM             VARCHAR(50)            NOT NULL,
    	  ROLE		             VARCHAR(50)            NOT NULL
    );

    insert into tbl_role (ID, PLATFORM, ROLE) values(1, 'APPSTORE', 'GUEST');
    insert into tbl_role (ID, PLATFORM, ROLE) values(2, 'APPSTORE', 'TENANT');
    insert into tbl_role (ID, PLATFORM, ROLE) values(3, 'APPSTORE', 'ADMIN');

    insert into tbl_role (ID, PLATFORM, ROLE) values(4, 'DEVELOPER', 'GUEST');
    insert into tbl_role (ID, PLATFORM, ROLE) values(5, 'DEVELOPER', 'TENANT');
    insert into tbl_role (ID, PLATFORM, ROLE) values(6, 'DEVELOPER', 'ADMIN');

    insert into tbl_role (ID, PLATFORM, ROLE) values(7, 'MECM', 'GUEST');
    insert into tbl_role (ID, PLATFORM, ROLE) values(8, 'MECM', 'TENANT');
    insert into tbl_role (ID, PLATFORM, ROLE) values(9, 'MECM', 'ADMIN');
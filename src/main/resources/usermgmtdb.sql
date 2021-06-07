
    CREATE TABLE if not exists tbl_tenant (
      	TENANTID            VARCHAR(100)      PRIMARY KEY NOT NULL,
      	USERNAME            VARCHAR(50)       NOT NULL,
      	PASSWORD            VARCHAR(100)      NOT NULL,
      	COMPANY             VARCHAR(50)       NULL,
      	TELEPHONENUMBER     VARCHAR(20)       NULL,
      	MAILADDRESS         VARCHAR(50)       NULL,
      	GENDER              VARCHAR(10)       NULL,
      	isallowed           boolean           NOT NULL,
      	CREATETIME          TIMESTAMP         NULL,
      	MODIFYTIME          TIMESTAMP         NULL,
      	PWEFFECTTIME        TIMESTAMP         NULL,
      	CONSTRAINT USERNAME UNIQUE (username),
      	CONSTRAINT TELEPHONENUMBER UNIQUE (telephonenumber),
      	CONSTRAINT MAILADDRESS UNIQUE (mailaddress)
    );

    CREATE TABLE if not exists tbl_tenant_role (
     	TENANTID            VARCHAR(100)      NOT NULL,
      	ROLEID              INTEGER           NOT NULL,
		primary key(TENANTID, ROLEID)
    );

    CREATE TABLE if not exists tbl_role (
        ID                   INTEGER                PRIMARY KEY NOT NULL,
    	PLATFORM             VARCHAR(50)            NOT NULL,
    	ROLE		         VARCHAR(50)            NOT NULL
    );
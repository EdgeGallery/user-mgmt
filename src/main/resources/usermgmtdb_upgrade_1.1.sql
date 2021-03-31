	alter table tbl_tenant alter column TELEPHONENUMBER drop NOT NULL;
	alter table tbl_tenant add MAILADDRESS VARCHAR(50) NULL;
	alter table tbl_tenant add CREATETIME TIMESTAMP NULL;
	alter table tbl_tenant add MODIFYTIME TIMESTAMP NULL;
	alter table tbl_tenant add CONSTRAINT MAILADDRESS UNIQUE (mailaddress);

	update tbl_tenant set MAILADDRESS = '13800000000@edgegallery.org' where USERNAME = 'guest';
	update tbl_tenant set MAILADDRESS = '13800000001@edgegallery.org' where USERNAME = 'admin';
	update tbl_tenant set CREATETIME = now(), MODIFYTIME = now();

# User Management 用户管理

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

用户管理模块为EdgeGallery平台提供了用户注册与登录、密码找回、帐号中心、用户列表管理、邮箱和手机验证等功能。

## 角色定义

用户管理模块定义了用户的角色与权限模型，包括：

- **租户** 系统内的普通用户，可以创建项目/上传App等操作，租户之间数据不能相互访问。

- **管理员** 系统管理员，可以对系统的应用数据进行管理和维护操作。

- **访客** 直接访问Portal时，默认是访客权限，只能浏览各平台的基本功能，不能做创建、编辑和删除等操作。

- **超级管理员** 系统内置一个默认用户：admin，作为平台的超级管理员。该用户可以管理其他用户，对平台已注册的所有用户进行统一管理，停用/启用用户，设置用户权限。

新注册的用户，默认是“租户”权限。“管理员”权限只能由超级管理admin在用户列表管理功能中，为租户提权获得。

## 主要特性
 
  用户管理模块提供的主要特性可以点击查看：[用户管理模块特性设计](http://docs.edgegallery.org/en/latest/Projects/User%20Management/User_Features.html)

## 本地运行User Management服务

  User Management对外提供restful接口，基于开源ServiceComb微服务框架进行开发，并且集成了Spring Boot框架。能够在本地直接编译运行启动微服务，方便使用者进行本地调试。


- ### 环境搭建与配置

  **1.环境准备：** 需要安装的工具及下载地址如下表所示。

  |  Name     | Version   | Link |
  |  ----     | ----  |  ---- |
  | OpenJDK1.8 |1.8.0 | [download](http://openjdk.java.net/install/)
  | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)
  | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)
  | Servicecomb Service-Center    | 1.3.2 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)
  | Postgres  | 10 or above |   [download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
  | Redis  | 3.2.100 or above | [download](https://github.com/microsoftarchive/redis/releases) |
  
  **2.源码下载：** 使用 git clone 或者下载压缩包的形式将User Management源代码下载到本地，默认master分支。
  首先需要下载依赖的父pom工程并编译：
  ```
  git clone https://gitee.com/edgegallery/eg-parent.git
  mvn install
  ```
  然后下载当前User Management工程：
  ```
  git clone https://gitee.com/edgegallery/user-mgmt.git
  ```
  
  **3.ServiceCenter配置：** User Management使用了开源的[ServiceComb](https://servicecomb.apache.org/)框架进行开发，服务在启动时会自动注册到指定的ServiceCenter，ServiceCenter会为服务提供注册与发现能力，供其他微服务进行调用。

  在启动User Management前需要先在本地启动ServiceCenter。
  
  - 首先[下载ServiceCenter](https://servicecomb.apache.org/cn/release/service-center-downloads/)，如Windows系统可以选择Windows的[Binary]版本，下载完成后解压；
  
  - 双击运行start-service-center.bat和start-frontend.bat即可在本地启动ServiceCenter和可视化面板，浏览器访问 http://127.0.0.1:30103 进行查看，ServiceCenter默认启动端口为30100；
  
  - 本地运行User Management服务，需要增加如下环境变量以连接SC(Service Center，即服务中心)：

  ```
    SC_ADDRESS：连接SC的地址。本地运行的SC默认为：http://127.0.0.1:30100
  ```
   
  **4.PostgreSQL数据库配置：** User Management使用了开源的[PostgreSQL](https://www.postgresql.org/)数据库存储用户的信息，本地运行时需要先安装PostgreSQL。
  
  - 推荐参考此处[安装和启动PostgreSQL](https://www.runoob.com/postgresql/windows-install-postgresql.html)，建议选择9.6或以上版本；
  
  - 使用文件`/src/main/resources/usermgmtdb.sql`初始化数据库表结构；

  - 使用文件`/src/main/resources/usermgmtdb-data.sql`初始化默认用户信息；
  
  - 本地运行User Management服务，需要增加如下环境变量以连接数据库：

  ```
    POSTGRES_IP：连接数据库的IP。本地运行的数据库，一般使用环回IP即可：127.0.0.1

    POSTGRES_PORT：连接数据库的端口。可以不配置，采取默认端口5432
  
    POSTGRES_USERNAME：数据库用户名

    POSTGRES_PASSWORD：数据库密码
  ```
  
  **5.Redis数据库配置：** User Management使用了Redis数据库存储图形验证码、手机/邮箱验证码，本地运行时需要先安装Redis。
  
  - 推荐参考此处[安装和启动Redis](https://www.runoob.com/redis/redis-install.html)；
  
  - 本地运行User Management服务，需要增加如下环境变量以连接Redis：
  
  ```
    REDIS_IP：连接Redis的IP。本地运行的Redis，一般使用环回IP即可：127.0.0.1

    REDIS_PORT：连接Redis的端口。可以不配置，采取默认端口6379

    REDIS_PASSWORD：连接Redis的密码。如果没有为Redis设置密码，可以不配置
  ```

  **6.业务平台Client配置：** User Management作为单点登录的Auth Server，各业务平台作为Auth Client。针对需要在本地运行的业务平台，User Management还需要增加对应该业务平台Client的配置信息。

  - 如果本地需要运行AppStore，User Management需要配置如下环境变量：

  ```
    OAUTH_APPSTORE_CLIENT_ID：AppStore业务平台的ClientID，配置为固定值appstore-fe。也可以不配置该变量，默认为appstore-fe

    OAUTH_APPSTORE_CLIENT_SECRET：AppStore业务平台的Client Secret，自行定义即可，但要注意AppStore业务平台运行时设置的Client Secret要与这里保持一致。

    OAUTH_APPSTORE_CLIENT_URL：连接AppStore业务平台的URL，如http://x.x.x.x:30091

    OAUTH_APPSTORE_CLIENT_ACCESS_URL：该配置是为代理访问模式定义的变量。正常访问模式下，与OAUTH_APPSTORE_CLIENT_URL保持一致即可
  ```

  - 类似的，如果本地要运行Developer、Mecm、ATP等平台，参考上述配置说明增加相应的环境变量配置。每个平台对应的环境变量名称请参考配置文件/src/main/resources/application.yaml中的oauth2.clients部分。
 
- ### 拷贝前端资源

  当前工程为User Management的后台，需要拷贝前端资源到后台工程的资源目录下。

  - 参考如下链接，编译User Management的前端资源：
    
    [https://gitee.com/edgegallery/user-mgmt-fe/blob/master/README.md](https://gitee.com/edgegallery/user-mgmt-fe/blob/master/README.md)

  - 编译成功后，请把dist目录下的内容拷贝到后台工程的/src/main/resources/static目录。

- ### 本地运行

  运行/src/main/java/org/edgegallery/user/auth/MainServer.java文件中的main函数就能启动User Management。

  启动成功后，可以单独访问User Management的界面：

    http://x.x.x.x:8067/index.html
  
## Kubernetes环境部署

  您还可以把User Management服务制作成Docker镜像部署在Linux环境和Kubernetes集群中运行。

  请参考[helm_charts](https://gitee.com/edgegallery/helm-charts/tree/master/edgegallery)项目
  
  

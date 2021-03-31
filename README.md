# User Management 用户管理

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

用户管理模块为EdgeGallery平台提供了用户注册与登录、密码找回、帐号中心、用户列表管理、邮箱和手机验证等功能。

## 角色定义

用户管理模块定义了用户的角色与权限模型，包括：

- **租户** 系统内的普通用户，可以创建项目/上传App等操作，租户之间数据不能相互访问。

- **管理员** 系统管理员，可以对系统的应用数据进行管理和维护操作。

- **访客** 直接访问Portal时，默认是访客权限，只能浏览各平台的基本功能，不能做创建、编辑和删除等操作。

- **超级管理员** 系统内置一个默认用户：admin，作为平台的超级管理员。该用户可以管理其它用户，对平台已注册的所有用户进行统一管理，停用/启用用户，设置用户权限。

新注册的用户，默认是“租户”权限。“管理员”权限只能由超级管理admin在用户列表管理功能中，为租户提权获得。

## 主要特性
 
  用户管理模块提供的主要特性可以点击查看：[用户管理模块特性设计](http://docs.edgegallery.org/en/latest/Projects/User%20Management/User_Features.html)

## 编译运行

  User Management对外提供restful接口，基于开源ServiceComb微服务框架进行开发，并且集成了Spring Boot框架。能够在本地直接编译运行启动微服务，方便使用者进行本地调试。并且还可以制作成Docker镜像部署在普通Linux环境和Kubernetes集群。

- ### 本地编译

  **1.环境准备：** 本地编译需要安装的工具包括jdk、maven、IDEA或Eclipse，此处默认已安装并配置好相关工具，如果没有安装，推荐参考此处[安装本地开发环境](https://docs.servicecomb.io/java-chassis/zh_CN/start/development-environment/)

  |  Name     | Version   | Link |
  |  ----     | ----  |  ---- |
  | OpenJDK1.8 |1.8.0 | [download](http://openjdk.java.net/install/)
  | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)
  | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)
  | Servicecomb Service-Center    | 1.3.2 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)
  | Postgres  | 9.6.17 or above |   [download](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
  | Redis  | 3.2.100 or above | [download](https://github.com/microsoftarchive/redis/releases) |
  
  **2.源码下载：** 使用 git clone 或者下载压缩包的形式将User Management源代码下载到本地，默认master分支
  ```sh
  git clone https://gitee.com/edgegallery/user-mgmt.git
  ```
  
  **3.ServiceCenter配置：** User Management使用了开源的[ServiceComb](https://servicecomb.apache.org/)框架进行开发，服务在启动时会自动注册到指定的ServiceCenter，ServiceCenter会为服务提供注册与发现能力，供其他微服务进行调用。
  在启动User Management前需要先在本地启动ServiceCenter。
  
  - 首先[下载ServiceCenter](https://servicecomb.apache.org/cn/release/service-center-downloads/)，如Windows系统可以选择Windows的[Binary]版本，下载完成后解压；
  
  - 双击运行start-service-center.bat和start-frontend.bat即可在本地启动ServiceCenter和可视化面板，浏览器访问 http://127.0.0.1:30103 进行查看，ServiceCenter默认启动端口为30100；
  
  - 本地运行user-mgmt时，需要配置service-center地址，修改配置文件/src/main/resources/application.yaml，例如：
  
    ```yaml
    servicecomb:
      service:
        registry:
          address: http://127.0.0.1:30100 #连接SC(Service Center,注册中心)的地址
    ```
   
  **4.PostgreSQL数据库配置：** User Management使用了开源的[PostgreSQL](https://www.postgresql.org/)数据库存储用户的信息，本地运行时需要先安装PostgreSQL。
  
  - 推荐参考此处[安装和启动PostgreSQL](https://www.runoob.com/postgresql/windows-install-postgresql.html)，建议选择9.6或以上版本；
  
  - 使用文件`/docker/user-mgmt-postgresql/postgres.sql`初始化数据库表结构；
  
  - 修改文件/src/main/resources/application.yaml，指向本地数据库，例如：
  
    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/${POSTGRES_DB_NAME:usermgmtdb}
        username: ${POSTGRES_USERNAME}
        password: ${POSTGRES_PASSWORD}
        driver-class-name: org.postgresql.Driver
    ```
  ${POSTGRES_DB_NAME:usermgmtdb}：替换为本地部署的db名称

  ${POSTGRES_USERNAME}：替换为本地数据库的用户名

  ${POSTGRES_PASSWORD}：替换为本地数据库的密码
  
  **5.Redis数据库配置：** User Management使用了Redis数据库临时存储手机验证码，本地运行时需要先安装Redis。
  
  - 推荐参考此处[安装和启动Redis](https://www.runoob.com/redis/redis-install.html)；
  
  - 修改文件/src/main/resources/application.properties，指向本地Redis，例如：
  
    ```properties
    ##### Redis config #####
    redis.ip=${REDIS_IP:127.0.0.1}
    redis.port=6379
    ```
  
  **6.开始运行：** 直接运行/src/main/java/org/mec/houp/user/MainServer.java文件中的main函数就能启动项目，此时可以尝试使用登录接口，但只能登录建立数据库表时默认插入的数据。
  如果想要正常使用其他接口，如注册、修改等，需要在公有云平台购买短信验证码服务，如华为公有云。修改文件/src/main/resources/init.properties中的sms前缀对应的各项参数为公有云提供的参数后，即可顺利使用User Management的全部接口。
  
- ### Kubernetes环境部署

  请参考[helm_charts](https://gitee.com/edgegallery/helm-charts/tree/master/edgegallery)项目
  
  

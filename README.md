# User Management 用户管理

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Jenkins](https://img.shields.io/jenkins/build?jobUrl=http://jenkins.edgegallery.org/view/GITEE-MEC-PLATFORM-BUILD/job/user-mgmt-docker-master-daily/)

  User Management 用户管理模块，为EdgeGallery提供了基本的用户增删改查功能，定义了用户的角色与权限，并且包含了两个关键特性：手机验证和单点登录（Single Sign On）能力。

## 角色定义

- **用户/租户** 系统内的用户，可以根据业务角色不同，指定不同的类型，当前有tenant（租户）和developer（开发者）；

- **用户管理员** 系统的用户管理员，可以对其他用户进行管理，修改用户信息与权限；

- **超级管理员** 系统的超级管理员，在数据库创建时插入，单表存储，无法被修改和删除。超级管理员可以指定和修改用户管理员及普通用户。

## 特性简介

- **手机验证**

  新用户在注册时，需要填写手机号和验证码，只有通过校验才能成功注册，一个手机号只能注册一个账户，忘记密码可以通过手机号找回；
  
  在登录时可以使用用户名与密码登录，也可以使用手机号与密码登录，手机号码与用户名都要求是唯一的。
  
  更多的架构设计可以点击[这里](http://docs.edgegallery.org/en/latest/)

- **单点登录**

  User Management 用户管理提供单点登录能力，被User Management信任的平台可以使用同一个认证服务器，在任一平台登录后，登录有效期内在其他平台无需重复登录即可正常进入操作界面。目前被信任的平台有Developer Portal（开发者平台）、Application Store（应用仓库）和 MEC Platform（MEC 平台）。
  
  更多的架构设计可以点击[这里](http://docs.edgegallery.org/en/latest/)

## 编译运行

  User Management对外提供restful接口，基于开源ServiceComb微服务框架进行开发，并且集成了Spring Boot框架。能够在本地直接编译运行启动微服务，方便使用者进行本地调试。并且还可以制作成Docker镜像部署在普通Linux环境和Kubernetes集群。

- ### 本地编译

  **1.环境准备：** 本地编译需要安装的工具包括jdk、maven、IDEA或Eclipse，此处默认已安装并配置好相关工具，如果没有安装，推荐参考此处[安装本地开发环境](https://docs.servicecomb.io/java-chassis/zh_CN/start/development-environment/)

  |  Name     | Version   | Link |
  |  ----     | ----  |  ---- |
  | JDK1.8 |1.8xxx | [download](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
  | MavApache Maven |3.6.3 | [download](https://maven.apache.org/download.cgi)
  | IntelliJ IDEA |Community |[download](https://www.jetbrains.com/idea/download/)
  | Servicecomb Service-Center    | 1.3.0 | [download](https://servicecomb.apache.org/cn/release/service-center-downloads/)
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
  
  

# Copyright 2020 Huawei Technologies Co., Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:8u201-jdk-alpine

## CREATE APP USER ##
# Create the home directory for the new app user.
RUN mkdir -p /usr/app

# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r app &&\
    useradd -r -g app -d /usr/app -s /sbin/nologin -c "Docker image user" app

# Create the working directory
RUN mkdir -p -m 750 /var/log/usermgmt &&\
    chown -hR app:app /var/log/usermgmt
	
ENV TZ='Asia/Shanghai'
ENV APP_HOME /usr/app
WORKDIR $APP_HOME

EXPOSE 8067
COPY --chown=app:app target/*.jar $APP_HOME/
ENTRYPOINT ["java","-jar", "user-mgmt-be.jar"]

# Change to the app user.
USER app
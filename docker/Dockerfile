FROM openjdk:8u201-jdk-alpine

## CREATE APP USER ##
# Create the home directory for the new app user.
RUN mkdir -p /usr/app

# Create an app user so our program doesn't run as root.
RUN apk update &&\
    apk add shadow &&\
    groupadd -r app &&\
    useradd -r -g app -d /usr/app -s /sbin/nologin -c "Docker image user" app

ENV TZ='Asia/Shanghai'
ENV APP_HOME /usr/app
WORKDIR $APP_HOME

EXPOSE 8067
COPY --chown=app:app target/*.jar $APP_HOME/
ENTRYPOINT ["java","-jar", "user-mgmt-be.jar"]

# Change to the app user.
USER app
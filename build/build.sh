#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_NAME="${1:?Need to set APP_NAME parameter (1th argument) to a non-empty value}"
DOMAIN_NAME="${2:?Need to set DOMAIN_NAME parameter (2nd argument) to a non-empty value}"
CONTACT_EMAIL="${3:?Need to set CONTACT_EMAIL parameter (3rd argument) to a non-empty value}"

if [ ! -d "$DIR/../$APP_NAME" ]
then
    printf "App name (1th parameter) cannot be located: $APP_NAME\n"
    printf "Directory: $DIR/../$APP_NAME\n"
    exit -1
fi

# build project
cd $DIR/../ && \
    mvn clean install

# build service
cp $DIR/../$APP_NAME/target/webstart.war $DIR/service/webstart.war && \
    cd $DIR/service && \
    docker build -t eaybars/ws-service . && \
    rm webstart.war

# build reverse proxy
cd $DIR/rproxy && \
    docker build -t eaybars/ws-proxy --build-arg DOMAIN_NAME=$DOMAIN_NAME --build-arg CONTACT_EMAIL=$CONTACT_EMAIL .
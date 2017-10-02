#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DOMAIN_NAME="${1:?Need to set DOMAIN_NAME parameter to a non-empty value}"


# build project
cd $DIR/../../ && \
    mvn clean install

# build docker images
cd $DIR/../auth && \
    docker build -t eaybars/ws-auth .

# build service
cp $DIR/../../ws-app-sgc/target/webstart.war $DIR../service/webstart.war && \
    cd $DIR/../service && \
    docker build -t eaybars/ws-service . && \
    rm webstart.war

# build nginx
cd $DIR/../nginx && \
    docker build -t eaybars/nginx .

# build nginx-ssl
cd $DIR/../nginx-ssl && \
    docker build -t eaybars/nginx-ssl .

# build reverse proxy
echo "Copying resources" && \
    mkdir $DIR/tmp/ && \
    cp $DIR/../auth/keycloak-location $DIR/tmp/keycloak-location && \
    cp $DIR/../auth/keycloak-upstream.conf $DIR/tmp/keycloak-upstream.conf && \
    cp $DIR/../service/service-location $DIR/tmp/service-location && \
    cp $DIR/../service/service-upstream.conf $DIR/tmp/service-upstream.conf

# create on the fly dockerfile for proxy
printf "FROM eaybars/nginx-ssl\n\
COPY keycloak-location /etc/nginx/locations/$DOMAIN_NAME/\n\
COPY service-location /etc/nginx/locations/$DOMAIN_NAME/\n\
COPY keycloak-upstream.conf /etc/nginx/conf.d/\n\
COPY service-upstream.conf /etc/nginx/conf.d/\n\
\n" >> $DIR/tmp/Dockerfile

cd $DIR/tmp && \
    docker build -t eaybars/ws-proxy .

cd $DIR && rm -rf $DIR/tmp
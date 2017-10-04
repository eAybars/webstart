FROM jboss/keycloak:3.2.1.Final

ENV MYSQL_VERSION 5.1.42
ENV MYSQL_HOST webstart-mysql
ENV MYSQL_DATABASE WEBSTART
ENV MYSQL_USER root
ENV MYSQL_PASSWORD changeit

# add mysql driver as module
RUN cd $HOME \
    && mkdir -p $JBOSS_HOME/modules/system/layers/base/com/mysql/jdbc/driver/main \
    && curl -L -o mysql.zip https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-$MYSQL_VERSION.zip \
    && unzip -p mysql.zip mysql-connector-java-$MYSQL_VERSION/mysql-connector-java-$MYSQL_VERSION-bin.jar > $JBOSS_HOME/modules/system/layers/base/com/mysql/jdbc/driver/main/mysql-connector-java-$MYSQL_VERSION-bin.jar \
    && rm mysql.zip \
    && cd $JBOSS_HOME/modules/system/layers/base/com/mysql/jdbc/driver/main/ \
    && touch module.xml \
    && echo '<?xml version="1.0" encoding="UTF-8"?>' >> module.xml \
    && echo '<module xmlns="urn:jboss:module:1.1" name="com.mysql.jdbc.driver">' >> module.xml \
    && echo '<resources>' >> module.xml \
    && echo "<resource-root path=\"mysql-connector-java-$MYSQL_VERSION-bin.jar\"/>" >> module.xml \
    && echo '</resources>' >> module.xml \
    && echo '<dependencies>' >> module.xml \
    && echo '<module name="javax.api"/>' >> module.xml \
    && echo '<module name="javax.transaction.api"/>' >> module.xml \
    && echo '</dependencies>' >> module.xml \
    && echo '</module>' >> module.xml

# create server install script file
RUN cd $JBOSS_HOME/bin \
        && touch app-install.cli \
        && echo "embed-server --server-config=standalone.xml" >> app-install.cli

# add reverse proxy settings
RUN echo "/socket-binding-group=standard-sockets/socket-binding=proxyhttps/:add(port=443)" >> $JBOSS_HOME/bin/app-install.cli && \
    echo "/subsystem=undertow/server=default-server/http-listener=default/:write-attribute(name=proxy-address-forwarding,value=true)" >> $JBOSS_HOME/bin/app-install.cli && \
    echo "/subsystem=undertow/server=default-server/http-listener=default/:write-attribute(name=redirect-socket,value=proxyhttps)" >> $JBOSS_HOME/bin/app-install.cli

# configure mysql as default datasource
RUN cd $JBOSS_HOME/bin \
    && echo "/subsystem=datasources/jdbc-driver=mysql:add(driver-name=mysql,jdbc-compliant=true,driver-module-name=com.mysql.jdbc.driver,driver-class-name=com.mysql.jdbc.Driver)" >> $JBOSS_HOME/bin/app-install.cli \
    && echo "/subsystem=datasources/data-source=KeycloakDS:remove" >> $JBOSS_HOME/bin/app-install.cli \
    && echo "/subsystem=datasources/data-source=KeycloakDS:add(driver-name=mysql,user-name=$MYSQL_USER,password=$MYSQL_PASSWORD,connection-url=\"jdbc:mysql://$MYSQL_HOST:3306/$MYSQL_DATABASE?useSSL=false\",min-pool-size=5,max-pool-size=15,jndi-name=java:jboss/datasources/KeycloakDS,enabled=true,validate-on-match=true,check-valid-connection-sql=\"/* ping */ SELECT 1\")" >> $JBOSS_HOME/bin/app-install.cli

# run install script file and cleanup afterwards
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/app-install.cli \
    && rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current/

# add realm data to import
ADD realm.json /opt/jboss/keycloak
ENV _JAVA_OPTIONS="-Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/opt/jboss/keycloak/realm.json -Dkeycloak.migration.strategy=IGNORE_EXISTING"

# example run with my sql
# docker network create webstart
# docker run -d --net webstart --name webstart-mysql -e MYSQL_ROOT_PASSWORD=changeit -e MYSQL_DATABASE=WEBSTART mysql
# docker run -d --net webstart --name webstart-auth eaybars/ws-auth
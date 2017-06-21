FROM jboss/wildfly:10.1.0.Final

# create wildfly configuration cli file
RUN touch $JBOSS_HOME/bin/app-install.cli \
    && echo "embed-server --server-config=standalone.xml" >> $JBOSS_HOME/bin/app-install.cli


# install Keycloak adapter
ENV KEYCLOAK_VERSION 2.4.0.Final
RUN curl -L http://downloads.jboss.org/keycloak/$KEYCLOAK_VERSION/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-$KEYCLOAK_VERSION.tar.gz | tar -zx -C $JBOSS_HOME \
    && echo "" >> $JBOSS_HOME/bin/app-install.cli \
    && cat $JBOSS_HOME/bin/adapter-install.cli >> $JBOSS_HOME/bin/app-install.cli

# run wildfly configuration file
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/app-install.cli

# clean up
RUN rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current/

# Default start server command
CMD $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0

# Deploy application
ADD webstart.war $JBOSS_HOME/standalone/deployments/
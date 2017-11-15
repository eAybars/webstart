FROM jboss/keycloak-adapter-wildfly:3.3.0.Final

COPY configure-standalone.cli $JBOSS_HOME/bin
COPY configure-standalone-ha.cli $JBOSS_HOME/bin

# rrun configurations and cleanup afterwards
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/configure-standalone.cli \
    && $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/configure-standalone-ha.cli \
    && rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current/

# Default start server command
CMD $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0

# Deploy application
ADD webstart.war $JBOSS_HOME/standalone/deployments/
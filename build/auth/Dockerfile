FROM jboss/keycloak:2.4.0.Final


ENV _JAVA_OPTIONS="-Dkeycloak.migration.action=import -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/opt/jboss/keycloak/realm.json -Dkeycloak.migration.strategy=IGNORE_EXISTING"

# add realm data to import
ADD realm.json /opt/jboss/keycloak
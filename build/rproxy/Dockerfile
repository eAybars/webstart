FROM eaybars/nginx-ssl

ARG DOMAIN_NAME
ARG CONTACT_EMAIL

ENV DOMAIN_NAME ${DOMAIN_NAME}
ENV CONTACT_EMAIL ${CONTACT_EMAIL}

COPY keycloak-location /etc/nginx/locations/$DOMAIN_NAME/
COPY service-location /etc/nginx/locations/$DOMAIN_NAME/
COPY keycloak-upstream.conf /etc/nginx/conf.d/
COPY service-upstream.conf /etc/nginx/conf.d/

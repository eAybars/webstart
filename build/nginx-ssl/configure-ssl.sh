#!/usr/bin/env bash

DOMAIN_NAME="${1:?Need to set DOMAIN_NAME environment variable or script parameter to a non-empty value}"

if [ ! -d "/etc/letsencrypt/live/$DOMAIN_NAME" ]
then
  CONTACT_EMAIL="${2:?Need to set CONTACT_EMAIL environment variable or script parameter to a non-empty value}"
  printf "Retrieving SSL certificate for $DOMAIN_NAME ...\n"
  touch /etc/nginx/cert.conf && \
    printf "worker_processes 1;\n\
    events { worker_connections  5; }\n\
    http { \n\
        server { \n\
            server_name $DOMAIN_NAME;\n\
            location ~ /.well-known { allow all; }\n\
        }\n\
    }\n" >> /etc/nginx/cert.conf && \
    nginx -c /etc/nginx/cert.conf && \
    certbot certonly -n --webroot --agree-tos --email $CONTACT_EMAIL --webroot-path=/usr/share/nginx/html/ -d $DOMAIN_NAME && \
    nginx -s stop && \
    rm --interactive=never /etc/nginx/cert.conf
else
    printf "Found SSL certificate for $DOMAIN_NAME\n"
fi


if [ ! -d "/etc/nginx/sites-available" ]; then
  mkdir /etc/nginx/sites-available
fi
if [ ! -d "/etc/nginx/sites-enabled" ]; then
  mkdir /etc/nginx/sites-enabled
fi
if [ ! -d "/etc/nginx/locations" ]; then
  mkdir /etc/nginx/locations
fi

if [ ! -f /etc/nginx/sites-available/$DOMAIN_NAME ]
then
    printf "Preparing configuration files for $DOMAIN_NAME\n"
    touch /etc/nginx/sites-available/$DOMAIN_NAME && \
    printf "server {\n\
                listen 80 default_server;\n\
                listen [::]:80 default_server;\n\
                server_name $DOMAIN_NAME;\n\
                return 301 https://\$server_name\$request_uri;\n\
            }\n\
            server { \n\
                listen 443 ssl default_server;\n\
                listen [::]:443 ssl default_server;\n\
                server_name $DOMAIN_NAME;\n\
                include /etc/nginx/locations/$DOMAIN_NAME/*;\n\
                ssl_protocols TLSv1 TLSv1.1 TLSv1.2;\n\
                ssl_prefer_server_ciphers on;\n\
                ssl_ciphers \"EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH\";\n\
                ssl_ecdh_curve secp384r1;\n\
                ssl_session_cache shared:SSL:10m;\n\
                ssl_session_tickets off;\n\
                ssl_stapling on;\n\
                ssl_stapling_verify on;\n\
                resolver 8.8.8.8 8.8.4.4 valid=300s;\n\
                resolver_timeout 5s;\n\
                add_header Strict-Transport-Security \"max-age=63072000; includeSubdomains\";\n\
                add_header X-Content-Type-Options nosniff;\n\
                ssl_dhparam /etc/ssl/certs/dhparam.pem;\n\
                ssl_certificate /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem;\n\
                ssl_certificate_key /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem;\n\
            }\n"     > /etc/nginx/sites-available/$DOMAIN_NAME && \
    printf "Successfully created configuration files for $DOMAIN_NAME\n"
else
    printf "Found configuration files for $DOMAIN_NAME\n"
fi

if [ ! -f /etc/nginx/sites-enabled/$DOMAIN_NAME ]
then
    printf "Activating configuration for $DOMAIN_NAME\n";
    ln -s /etc/nginx/sites-available/$DOMAIN_NAME /etc/nginx/sites-enabled/$DOMAIN_NAME &&
        printf "Configuration for $DOMAIN_NAME is now active\n";
else
    printf "Configuration for $DOMAIN_NAME is active\n";
fi

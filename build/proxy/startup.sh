#!/usr/bin/env bash

"${DOMAIN_NAME:?Need to set DOMAIN_NAME environment variable to a non-empty value}"

if [ ! -d "/etc/letsencrypt/live/$DOMAIN_NAME" ]; then
  "${CONTACT_EMAIL:?Need to set CONTACT_EMAIL environment variable to a non-empty value}"
  touch /etc/nginx/cert.conf && \
    printf "worker_processes 1;\n\
    events { worker_connections  5; }\n\
    http { \n\
        server { \n\
            server_name $DOMAIN_NAME;\n\
            location ~ /.well-known { allow all; }\n\
        }\n\
    }" >> /etc/nginx/cert.conf && \
    nginx -c /etc/nginx/cert.conf && \
    certbot certonly -n --webroot --agree-tos --email $CONTACT_EMAIL --webroot-path=/usr/share/nginx/html/ -d $DOMAIN_NAME && \
    nginx -s stop && \
    rm --interactive=never /etc/nginx/cert.conf

    openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048
fi


if [ ! -d "/etc/nginx/sites-available" ]; then
  mkdir /etc/nginx/sites-available /etc/nginx/sites-enabled

  # override main configuration file
  printf "worker_processes auto;\n\
            events {\n\
              worker_connections  1024;\n\
            }\n\
            http {\n\
              sendfile on;\n\
              tcp_nopush on;\n\
              tcp_nodelay on;\n\
              keepalive_timeout 65;\n\
              types_hash_max_size 2048;\n\
              include /etc/nginx/mime.types;\n\
              default_type application/octet-stream;\n\
              gzip on;\n\
              gzip_disable "msie6";\n\
              gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;\n\
              include /etc/nginx/conf.d/*.conf;\n\
              include /etc/nginx/sites-enabled/*;\n\
            }\n" > /etc/nginx/nginx.conf

  touch /etc/nginx/sites-available/default_server && \
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
                include /etc/nginx/default_locations/*;\n\
                location ~ /.well-known { allow all; } \n\
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
            }"     > /etc/nginx/sites-available/default_server && \
    ln -s /etc/nginx/sites-available/default_server /etc/nginx/sites-enabled/default_server
fi

nginx -g "daemon off;"
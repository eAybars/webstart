FROM eaybars/nginx

# install certbot to obtain ssl certificate from Lets Encrypt
RUN yum install -y certbot && \
    yum install -y cronie cronie-anacron && \
    yum clean all

# Generate Strong Diffie-Hellman Group
RUN openssl dhparam -out /etc/ssl/certs/dhparam.pem 2048

# add cron job for renewing certificates automatically
RUN touch /etc/cron.daily/renew_cert.sh && \
    printf "certbot renew --quiet\n" > /etc/cron.daily/renew_cert.sh

# enable cron service
RUN systemctl enable crond.service

# override main configuration file
RUN printf "worker_processes auto;\n\
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

COPY configure-ssl.sh /etc/nginx

RUN chmod 777 /etc/nginx/configure-ssl.sh

ENTRYPOINT /etc/nginx/configure-ssl.sh $DOMAIN_NAME $CONTACT_EMAIL && \
    nginx -g "daemon off;"

# example way to run the system
# docker run -d --name ssl-proxy -e DOMAIN_NAME=novalab.com.tr -e CONTACT_EMAIL=info@novalab.com.tr -v /host/directory:/etc/letsencrypt eaybars/ssql-nginx
FROM eaybars/nginx

# install certbot to obtain ssl certificate from Lets Encrypt
RUN yum install -y certbot && \
    yum install -y cronie && \
    yum clean all

RUN touch /etc/cron.daily/renew_cert.sh && \
    printf "certbot renew --quiet" > /etc/cron.daily/renew_cert.sh

COPY startup.sh /etc/nginx

ENTRYPOINT startup.sh

# example way to run the system
# docker run -d --name ssl-proxy -e DOMAIN_NAME=novalab.com.tr -e CONTACT_EMAIL=info@novalab.com.tr -v /host/directory:/etc/letsencrypt eaybars/ssql-nginx
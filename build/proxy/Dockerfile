FROM novalab/nginx

# install certbot to obtain ssl certificate from Lets Encrypt
RUN yum install -y certbot && yum clean all

COPY startup.sh /etc/nginx

ENTRYPOINT startup.sh
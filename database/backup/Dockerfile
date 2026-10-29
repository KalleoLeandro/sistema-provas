FROM mysql:8.4
USER root
RUN microdnf install -y cronie util-linux tzdata && microdnf clean all \
    && ln -snf /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime
COPY entrypoint.sh backup.sh backup-healthcheck.sh /usr/local/bin/
COPY backup.cron /etc/cron.d/database-backup
RUN sed -i 's/\r$//' /usr/local/bin/entrypoint.sh /usr/local/bin/backup.sh /usr/local/bin/backup-healthcheck.sh /etc/cron.d/database-backup \
    && chmod 0755 /usr/local/bin/entrypoint.sh /usr/local/bin/backup.sh /usr/local/bin/backup-healthcheck.sh \
    && chmod 0644 /etc/cron.d/database-backup
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["crond", "-n", "-m", "off"]

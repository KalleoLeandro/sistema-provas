#!/bin/bash
set -euo pipefail
umask 077
: "${MYSQL_HOST:?Informe MYSQL_HOST}"
: "${MYSQL_DATABASE:?Informe MYSQL_DATABASE}"
: "${MYSQL_USER:?Informe MYSQL_USER}"
: "${MYSQL_PASSWORD:?Informe MYSQL_PASSWORD}"
mkdir -p /backups /run/backup
# Cron inicia com ambiente próprio. Bash serializa os valores sem perder caracteres especiais.
# O arquivo fica somente no container, com acesso restrito, e não integra o dump.
declare -p MYSQL_HOST MYSQL_DATABASE MYSQL_USER MYSQL_PASSWORD > /run/backup/environment
chmod 600 /run/backup/environment
if ! /usr/local/bin/backup.sh; then
    echo "[backup] Falha no backup inicial; o anterior foi preservado. O cron tentará novamente."
fi
echo "[backup] Agendamento ativo: 00h, 04h, 08h, 12h, 16h e 20h (America/Sao_Paulo)."
exec "$@"

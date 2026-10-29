#!/bin/bash
set -euo pipefail
umask 077
if [[ -r /run/backup/environment ]]; then
    source /run/backup/environment
fi
: "${MYSQL_HOST:?Informe MYSQL_HOST}"
: "${MYSQL_DATABASE:?Informe MYSQL_DATABASE}"
: "${MYSQL_USER:?Informe MYSQL_USER}"
: "${MYSQL_PASSWORD:?Informe MYSQL_PASSWORD}"
mkdir -p /backups
exec 9>/backups/.backup.lock
if ! flock -n 9; then
    echo "[backup] Já existe um dump em execução; esta chamada foi ignorada."
    exit 0
fi
temporary=/backups/.dump.sql.tmp
cleanup() {
    result=$?
    rm -f -- "$temporary"
    if (( result != 0 )); then
        echo "[backup] ERRO: o novo dump não foi concluído; dump.sql anterior preservado." >&2
    fi
}
trap cleanup EXIT
trap 'exit 1' HUP INT TERM
echo "[backup] Iniciando dump em $(date --iso-8601=seconds)."
export MYSQL_PWD="$MYSQL_PASSWORD"
mysqldump --protocol=TCP --host="$MYSQL_HOST" --user="$MYSQL_USER" \
    --single-transaction --quick --skip-lock-tables \
    --no-tablespaces --set-gtid-purged=OFF --hex-blob --default-character-set=utf8mb4 \
    --comments --dump-date --databases "$MYSQL_DATABASE" > "$temporary"
test -s "$temporary"
tail -n 5 "$temporary" | grep -q '^-- Dump completed on '
# Grava o conteúdo antes de publicar. O rename no mesmo diretório substitui o
# arquivo anterior sem criar um intervalo em que dump.sql deixa de existir.
sync "$temporary"
mv -f -- "$temporary" /backups/dump.sql
echo "[backup] SUCESSO: dump.sql atualizado em $(date --iso-8601=seconds) ($(stat -c %s /backups/dump.sql) bytes)."

#!/bin/bash
# Executar em um container descartável, com /backups em tmpfs, nunca no serviço ativo.
set -euo pipefail
export MYSQL_HOST=test MYSQL_DATABASE=test MYSQL_USER=test MYSQL_PASSWORD=test
script=/usr/local/bin/backup.sh
mysqldump() { printf 'backup completo\n-- Dump completed on 2026-01-01 00:00:00\n'; }
export -f mysqldump
"$script"
baseline=$(sha256sum /backups/dump.sql)
/usr/local/bin/backup-healthcheck.sh
mysqldump() { printf 'dump parcial\n'; return 1; }
export -f mysqldump
if "$script"; then echo 'ERRO: dump incompleto foi aceito'; exit 1; fi
test "$(sha256sum /backups/dump.sql)" = "$baseline"
test ! -e /backups/.dump.sql.tmp
mysqldump() { printf 'dump sem marcador de conclusão\n'; }
export -f mysqldump
if "$script"; then echo 'ERRO: dump sem rodapé foi aceito'; exit 1; fi
test "$(sha256sum /backups/dump.sql)" = "$baseline"
mysqldump() { return 0; }
export -f mysqldump
if "$script"; then echo 'ERRO: dump vazio foi aceito'; exit 1; fi
test "$(sha256sum /backups/dump.sql)" = "$baseline"
# Outra chamada não pode sobrescrever um backup que já está em execução.
exec 8>/backups/.backup.lock
flock -n 8
"$script"
test "$(sha256sum /backups/dump.sql)" = "$baseline"
flock -u 8
mysqldump() { printf 'novo backup completo\n-- Dump completed on 2026-01-01 04:00:00\n'; }
export -f mysqldump
"$script"
test "$(sha256sum /backups/dump.sql)" != "$baseline"
test "$(find /backups -name '*.sql' | wc -l)" -eq 1
test ! -e /backups/.dump.sql.tmp
echo 'SUCESSO: substituição, falhas, arquivo vazio, rodapé e concorrência validados.'

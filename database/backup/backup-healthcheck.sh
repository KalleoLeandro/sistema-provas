#!/bin/bash
set -euo pipefail
test -s /backups/dump.sql
# Uma hora de tolerância após o intervalo previsto de quatro horas.
age=$(( $(date +%s) - $(stat -c %Y /backups/dump.sql) ))
(( age >= 0 && age <= 18000 ))

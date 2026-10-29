# Backup agendado do MySQL

O serviço `backup` do Docker Compose usa o cliente MySQL 8.4 e cron. Gera um
dump ao iniciar e depois às **00h, 04h, 08h, 12h, 16h e 20h**, no fuso
`America/Sao_Paulo`. O servidor precisa estar ligado e o serviço em execução.

O arquivo fica no host em **`database/backups/dump.sql`**, fora do container do
MySQL e do volume de dados. Para escolher outro diretório do servidor, configure
`BACKUP_DIRETORIO` no `.env` antes de recriar o serviço. O destino deve ser um
diretório persistente e gravável. Essa pasta já é ignorada pelo Git no destino
padrão; use um diretório fora do repositório ao personalizar o caminho.

Só um backup concluído é mantido. Durante a execução existe também
`.dump.sql.tmp`. O script exige código de saída zero do `mysqldump`, arquivo não
vazio e rodapé de conclusão; sincroniza o conteúdo e substitui `dump.sql` com um
rename no mesmo diretório. Assim, o anterior só é removido ao publicar o novo.
Falhas preservam `dump.sql` e removem o temporário. Um lock impede execuções
simultâneas. Uma interrupção abrupta pode deixar o temporário, que será
sobrescrito na próxima execução.

O dump usa `--single-transaction`, inclui estrutura e dados do banco configurado
e não requer a senha root. Evite alterações de estrutura durante sua geração.
Essa cópia cobre o **MySQL**; as chaves de `back-end/keys`, arquivos do projeto e
o estado do Redis não integram o SQL. Guarde as chaves separadamente para
recuperar os tokens cifrados após uma restauração. A cópia fica no mesmo servidor;
não é enviada para outro servidor ou armazenamento remoto.

## Operação

```powershell
docker compose up -d --build backup
docker compose logs --tail=30 backup
docker compose exec -T backup /usr/local/bin/backup.sh
docker compose ps backup
```

O healthcheck marca o serviço como não saudável quando não há dump ou quando
o último arquivo tem mais de cinco horas. Consulte os logs para investigar;
o cron continuará tentando a cada quatro horas.

## Restauração manual

A restauração substitui os dados do banco de destino. Execute somente no banco
que deseja restaurar, com as aplicações paradas e após conferir o arquivo.
Se personalizou `BACKUP_DIRETORIO`, ajuste o caminho de origem abaixo.

```powershell
docker compose cp ./database/backups/dump.sql banco:/tmp/restaurar.sql
docker compose exec -T banco sh -c 'export MYSQL_PWD=$MYSQL_PASSWORD; mysql -u$MYSQL_USER $MYSQL_DATABASE < /tmp/restaurar.sql'
docker compose exec -T banco rm -f /tmp/restaurar.sql
```

## Teste de preservação do backup

O teste usa somente um container descartável com diretório em memória:

```powershell
Get-Content -Raw database/backup/test.sh | docker run --rm -i --tmpfs /backups --entrypoint /bin/bash sistema-provas-backup -c "sed 's/\r$//' | /bin/bash"
```

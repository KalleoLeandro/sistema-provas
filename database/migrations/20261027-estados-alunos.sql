-- Executar uma vez em bancos existentes; instalações novas usam init.sql.
ALTER TABLE alunos_2026 DROP CHECK ck_alunos_status, ADD CONSTRAINT ck_alunos_status CHECK (status IN ('M','T','R','D'));

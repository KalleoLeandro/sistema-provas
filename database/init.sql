CREATE DATABASE IF NOT EXISTS sgp
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE sgp;

CREATE TABLE IF NOT EXISTS materias (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_materias_nome UNIQUE (nome)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS usuarios_administrativos (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  login VARCHAR(120) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  master BOOLEAN NOT NULL DEFAULT FALSE,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uk_usuarios_administrativos_login UNIQUE (login)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Conta inicial: não modifica nome, senha ou status de uma conta existente.
USE sgp;

INSERT INTO usuarios_administrativos (nome, login, senha_hash, master)
SELECT 'Master User', 'administrador', 'b50ff0081fde93beefeef82d29852d61647af4c8c0afcfbc6373eed7506edc1cb917d0cf4cce9a8db0c7888a66364c73', TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM usuarios_administrativos WHERE login = 'administrador'
);


-- Spring Session JDBC: o sufixo _ATTRIBUTES e as colunas sao definidos pelo framework.
CREATE TABLE IF NOT EXISTS sessoes (
  PRIMARY_ID CHAR(36) NOT NULL,
  SESSION_ID CHAR(36) NOT NULL,
  CREATION_TIME BIGINT NOT NULL,
  LAST_ACCESS_TIME BIGINT NOT NULL,
  MAX_INACTIVE_INTERVAL INT NOT NULL,
  EXPIRY_TIME BIGINT NOT NULL,
  PRINCIPAL_NAME VARCHAR(100),
  PRIMARY KEY (PRIMARY_ID),
  UNIQUE KEY uk_sessoes_id (SESSION_ID),
  KEY ix_sessoes_expiracao (EXPIRY_TIME),
  KEY ix_sessoes_usuario (PRINCIPAL_NAME)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS sessoes_ATTRIBUTES (
  SESSION_PRIMARY_ID CHAR(36) NOT NULL,
  ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
  ATTRIBUTE_BYTES BLOB NOT NULL,
  PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
  CONSTRAINT fk_atributos_sessao FOREIGN KEY (SESSION_PRIMARY_ID)
    REFERENCES sessoes(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS turmas (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  ensino CHAR(1) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  ano TINYINT UNSIGNED NOT NULL,
  letra CHAR(1) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_turmas_identificacao UNIQUE (ensino, ano, letra),
  CONSTRAINT ck_turmas_ensino CHECK (ensino IN ('F', 'M')),
  CONSTRAINT ck_turmas_ano CHECK (ano BETWEEN 1 AND 9),
  CONSTRAINT ck_turmas_letra CHECK (ASCII(letra) BETWEEN 65 AND 90)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;


USE sgp;

CREATE TABLE IF NOT EXISTS professores (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  login VARCHAR(120) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uk_professores_login UNIQUE (login)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS professores_turmas (
  professor_id BIGINT UNSIGNED NOT NULL,
  turma_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (professor_id, turma_id),
  KEY ix_professores_turmas_turma (turma_id),
  CONSTRAINT fk_professores_turmas_professor FOREIGN KEY (professor_id) REFERENCES professores (id) ON DELETE CASCADE,
  CONSTRAINT fk_professores_turmas_turma FOREIGN KEY (turma_id) REFERENCES turmas (id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


USE sgp;

CREATE TABLE IF NOT EXISTS professores_materias (
  professor_id BIGINT UNSIGNED NOT NULL,
  materia_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (professor_id, materia_id),
  CONSTRAINT fk_professores_materias_professor FOREIGN KEY (professor_id) REFERENCES professores (id) ON DELETE CASCADE,
  CONSTRAINT fk_professores_materias_materia FOREIGN KEY (materia_id) REFERENCES materias (id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS professores_materias_turmas (
  professor_id BIGINT UNSIGNED NOT NULL,
  materia_id BIGINT UNSIGNED NOT NULL,
  turma_id BIGINT UNSIGNED NOT NULL,
  ativo BOOLEAN NOT NULL,
  reserva_ativa TINYINT GENERATED ALWAYS AS (CASE WHEN ativo THEN 1 ELSE NULL END) STORED,
  PRIMARY KEY (professor_id, materia_id, turma_id),
  CONSTRAINT uk_materia_turma_ativa UNIQUE (materia_id, turma_id, reserva_ativa),
  CONSTRAINT fk_professor_materia_turma_materia FOREIGN KEY (professor_id, materia_id) REFERENCES professores_materias (professor_id, materia_id) ON DELETE CASCADE,
  CONSTRAINT fk_professor_materia_turma_turma FOREIGN KEY (turma_id) REFERENCES turmas (id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Os vínculos antigos de professores_turmas são preservados: não é possível
-- deduzir qual matéria cada professor ministra a partir desses registros.


USE sgp;

CREATE TABLE IF NOT EXISTS questoes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  professor_id BIGINT UNSIGNED NOT NULL,
  materia_id BIGINT UNSIGNED NOT NULL,
  ensino CHAR(1) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  ano TINYINT UNSIGNED NOT NULL,
  abrangencia VARCHAR(6) NOT NULL DEFAULT 'turmas',
  tipo VARCHAR(10) NOT NULL,
  peso DECIMAL(4,1) NOT NULL,
  enunciado LONGTEXT NOT NULL,
  alternativa_a LONGTEXT,
  alternativa_b LONGTEXT,
  alternativa_c LONGTEXT,
  alternativa_d LONGTEXT,
  alternativa_e LONGTEXT,
  resposta_correta CHAR(1),
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_questoes_professor (professor_id),
  KEY ix_questoes_materia_ano (materia_id, ensino, ano),
  CONSTRAINT fk_questoes_professor FOREIGN KEY (professor_id) REFERENCES professores (id),
  CONSTRAINT fk_questoes_materia FOREIGN KEY (materia_id) REFERENCES materias (id),
  CONSTRAINT ck_questoes_ensino CHECK (ensino IN ('F','M')),
  CONSTRAINT ck_questoes_ano CHECK (ano BETWEEN 1 AND 9),
  CONSTRAINT ck_questoes_abrangencia CHECK (abrangencia IN ('turmas','ano')),
  CONSTRAINT ck_questoes_tipo CHECK (tipo IN ('objetiva', 'discursiva')),
  CONSTRAINT ck_questoes_peso CHECK (peso > 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS questoes_turmas (
  questao_id BIGINT UNSIGNED NOT NULL,
  turma_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (questao_id, turma_id),
  CONSTRAINT fk_questoes_turmas_questao FOREIGN KEY (questao_id) REFERENCES questoes (id) ON DELETE CASCADE,
  CONSTRAINT fk_questoes_turmas_turma FOREIGN KEY (turma_id) REFERENCES turmas (id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;


USE sgp;
CREATE TABLE IF NOT EXISTS questoes_versoes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  questao_id BIGINT UNSIGNED NOT NULL,
  numero INT UNSIGNED NOT NULL,
  utilizada_em_prova_respondida BOOLEAN NOT NULL DEFAULT FALSE,
  revisao BIGINT UNSIGNED NOT NULL DEFAULT 1,
  materia_id BIGINT UNSIGNED NOT NULL,
  ensino CHAR(1) NOT NULL,
  ano TINYINT UNSIGNED NOT NULL,
  abrangencia VARCHAR(6) NOT NULL,
  turmas JSON NOT NULL,
  tipo VARCHAR(10) NOT NULL,
  peso DECIMAL(4,1) NOT NULL,
  enunciado LONGTEXT NOT NULL,
  alternativa_a LONGTEXT,
  alternativa_b LONGTEXT,
  alternativa_c LONGTEXT,
  alternativa_d LONGTEXT,
  alternativa_e LONGTEXT,
  resposta_correta CHAR(1),
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_questoes_versoes_numero (questao_id, numero),
  CONSTRAINT fk_questoes_versoes_questao FOREIGN KEY (questao_id) REFERENCES questoes (id),
  CONSTRAINT ck_questoes_versoes_numero CHECK (numero > 0)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Cada questão existente passa a ter uma versão inicial sem alterar seu conteúdo.
INSERT INTO questoes_versoes (questao_id, numero, materia_id, ensino, ano, abrangencia, turmas, tipo, peso, enunciado,
  alternativa_a, alternativa_b, alternativa_c, alternativa_d, alternativa_e, resposta_correta)
SELECT q.id, 1, q.materia_id, q.ensino, q.ano, q.abrangencia,
  COALESCE((SELECT JSON_ARRAYAGG(qt.turma_id) FROM questoes_turmas qt WHERE qt.questao_id = q.id), JSON_ARRAY()),
  q.tipo, q.peso, q.enunciado, q.alternativa_a, q.alternativa_b, q.alternativa_c, q.alternativa_d, q.alternativa_e, q.resposta_correta
FROM questoes q WHERE NOT EXISTS (SELECT 1 FROM questoes_versoes v WHERE v.questao_id = q.id);

USE sgp;
CREATE TABLE IF NOT EXISTS alunos_2026 (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  cgm VARCHAR(30) NOT NULL,
  turma_id BIGINT UNSIGNED NOT NULL,
  numero INT UNSIGNED NULL,
  status CHAR(1) CHARACTER SET ascii COLLATE ascii_bin NOT NULL DEFAULT 'M',
  PRIMARY KEY (id),
  CONSTRAINT uk_alunos_cgm UNIQUE (cgm),
  CONSTRAINT ck_alunos_status CHECK (status IN ('M','T','R','D')),
  CONSTRAINT ck_alunos_numero CHECK (numero IS NULL OR numero>0),
  KEY ix_alunos_turma_nome (turma_id,nome,id),
  CONSTRAINT fk_alunos_turma FOREIGN KEY (turma_id) REFERENCES turmas (id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE sgp;
CREATE TABLE IF NOT EXISTS provas_liberacoes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  professor_id BIGINT UNSIGNED NOT NULL,
  materia_id BIGINT UNSIGNED NOT NULL,
  turma_id BIGINT UNSIGNED NOT NULL,
  titulo VARCHAR(120) NOT NULL,
  duracao_minutos SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  token_hash VARCHAR(255) NULL,
  token_cifrado VARCHAR(2048) NULL,
  CONSTRAINT ck_liberacoes_duracao CHECK(duracao_minutos BETWEEN 1 AND 480),
  disponivel_em DATETIME(3) NOT NULL,
  encerra_em DATETIME(3) NOT NULL,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY ix_liberacoes_professor_turma (professor_id,turma_id,id),
  CONSTRAINT fk_liberacoes_professor FOREIGN KEY (professor_id) REFERENCES professores(id),
  CONSTRAINT fk_liberacoes_materia FOREIGN KEY (materia_id) REFERENCES materias(id),
  CONSTRAINT fk_liberacoes_turma FOREIGN KEY (turma_id) REFERENCES turmas(id),
  CONSTRAINT ck_liberacoes_periodo CHECK (encerra_em > disponivel_em)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE sgp;
-- Configuração adicionada diretamente na tabela de liberações acima.
CREATE TABLE IF NOT EXISTS provas_tentativas (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 liberacao_id BIGINT UNSIGNED NOT NULL, aluno_id BIGINT UNSIGNED NOT NULL,
 titulo VARCHAR(120) NOT NULL,materia VARCHAR(120) NOT NULL,
 iniciada_em DATETIME(3) NOT NULL, encerra_em DATETIME(3) NOT NULL,
 submetida_em DATETIME(3) NULL, automatica BOOLEAN NOT NULL DEFAULT FALSE,
 UNIQUE KEY uk_tentativa_aluno_liberacao(aluno_id,liberacao_id),
 KEY ix_tentativas_prazo(submetida_em,encerra_em),
 CONSTRAINT fk_tentativa_liberacao FOREIGN KEY(liberacao_id) REFERENCES provas_liberacoes(id),
 CONSTRAINT fk_tentativa_aluno FOREIGN KEY(aluno_id) REFERENCES alunos_2026(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS provas_tentativas_questoes (
 tentativa_id BIGINT UNSIGNED NOT NULL,numero TINYINT UNSIGNED NOT NULL,
 questao_id BIGINT UNSIGNED NOT NULL,versao_id BIGINT UNSIGNED NOT NULL,
 tipo VARCHAR(10) NOT NULL,peso DECIMAL(4,1) NOT NULL,enunciado LONGTEXT NOT NULL,
 alternativa_a LONGTEXT,alternativa_b LONGTEXT,alternativa_c LONGTEXT,alternativa_d LONGTEXT,alternativa_e LONGTEXT,
 resposta_correta CHAR(1),resposta TEXT NULL,revisao BIGINT UNSIGNED NOT NULL DEFAULT 0,pontuacao DECIMAL(5,2) NULL,
 feedback TEXT NULL,correcao_revisao BIGINT UNSIGNED NOT NULL DEFAULT 0,corrigida_em DATETIME(3) NULL,corrigida_por BIGINT UNSIGNED NULL,
 CONSTRAINT fk_correcao_professor FOREIGN KEY(corrigida_por) REFERENCES professores(id),
 CONSTRAINT ck_correcao_pontuacao CHECK(pontuacao IS NULL OR (pontuacao>=0 AND pontuacao<=peso)),
 PRIMARY KEY(tentativa_id,numero),UNIQUE KEY uk_tentativa_questao(tentativa_id,questao_id),
 CONSTRAINT fk_tentativa_questao_tentativa FOREIGN KEY(tentativa_id) REFERENCES provas_tentativas(id),
 CONSTRAINT fk_tentativa_questao_origem FOREIGN KEY(questao_id) REFERENCES questoes(id),
 CONSTRAINT fk_tentativa_questao_versao FOREIGN KEY(versao_id) REFERENCES questoes_versoes(id),
 CONSTRAINT ck_tentativa_numero CHECK(numero BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Assuntos de questões e provas

CREATE TABLE assuntos (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,materia_id BIGINT UNSIGNED NOT NULL,nome VARCHAR(120) NOT NULL,
 PRIMARY KEY(id),UNIQUE KEY uk_assunto_nome(materia_id,nome),UNIQUE KEY uk_assunto_materia(id,materia_id),
 CONSTRAINT fk_assunto_materia FOREIGN KEY(materia_id) REFERENCES materias(id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER TABLE questoes ADD COLUMN assunto_id BIGINT UNSIGNED NULL;
ALTER TABLE questoes_versoes ADD COLUMN assunto_id BIGINT UNSIGNED NULL;
CREATE TABLE provas_liberacoes_assuntos (
 liberacao_id BIGINT UNSIGNED NOT NULL,assunto_id BIGINT UNSIGNED NOT NULL,PRIMARY KEY(liberacao_id,assunto_id),
 CONSTRAINT fk_liberacao_assunto_liberacao FOREIGN KEY(liberacao_id) REFERENCES provas_liberacoes(id) ON DELETE CASCADE,
 CONSTRAINT fk_liberacao_assunto_assunto FOREIGN KEY(assunto_id) REFERENCES assuntos(id)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
INSERT INTO assuntos(materia_id,nome) SELECT DISTINCT materia_id,'Geral' FROM (SELECT materia_id FROM questoes UNION SELECT materia_id FROM provas_liberacoes UNION SELECT materia_id FROM questoes_versoes) materias_existentes;
UPDATE questoes q JOIN assuntos a ON a.materia_id=q.materia_id AND a.nome='Geral' SET q.assunto_id=a.id;
UPDATE questoes_versoes q JOIN assuntos a ON a.materia_id=q.materia_id AND a.nome='Geral' SET q.assunto_id=a.id;
INSERT INTO provas_liberacoes_assuntos(liberacao_id,assunto_id) SELECT l.id,a.id FROM provas_liberacoes l JOIN assuntos a ON a.materia_id=l.materia_id AND a.nome='Geral';
ALTER TABLE questoes MODIFY assunto_id BIGINT UNSIGNED NOT NULL,ADD CONSTRAINT fk_questao_assunto FOREIGN KEY(assunto_id,materia_id) REFERENCES assuntos(id,materia_id),ADD INDEX ix_questao_sorteio(professor_id,materia_id,ensino,ano,assunto_id,peso);
ALTER TABLE questoes_versoes MODIFY assunto_id BIGINT UNSIGNED NOT NULL,ADD CONSTRAINT fk_versao_assunto FOREIGN KEY(assunto_id,materia_id) REFERENCES assuntos(id,materia_id);

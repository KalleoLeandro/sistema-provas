# Back-end ESFINORA

## Usuário master

O `database/init.sql` cadastra o usuário **Master User**, login `administrador`, em `sgp.usuarios_administrativos`. O script `database/usuario-master.sql` contém somente esse cadastro para aplicação em bancos já inicializados. Ambos preservam uma conta existente com o mesmo login.

A senha inicial informada na configuração foi convertida para um hash PBKDF2-HMAC-SHA256, com salt aleatório de 16 bytes e 310.000 iterações, compatível com `PasswordConfig`. A senha original não é gravada nos arquivos do projeto. Para uma nova senha, use `PasswordEncoder.encode`, sem criptografia reversível.

O fluxo segue `LoginController` → `LoginService` → `UsuarioAdministrativoRepository`. A entidade `UsuarioAdministrativo` representa a conta. O service valida o hash e bloqueia contas inativas; o controller cria a sessão HTTP. `SecurityConfig` configura CSRF e proteção das rotas. A integração da tela Angular com essa autenticação será feita em outra etapa; ela ainda não envia credenciais ao back-end.

O script inicial só executa automaticamente em um volume MySQL vazio. Para cadastrar em um banco existente, execute a partir da pasta raiz do projeto, no PowerShell:

```powershell
Get-Content -Raw database/usuario-master.sql | docker compose exec -T banco sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'
```

## Chaves locais

`keys/privada.pem` (PKCS#8) e `keys/publica.pem` (SPKI) formam um par RSA de 3072 bits. Para gerar em outro ambiente:

```powershell
node gerar-chaves.mjs
```

O script não sobrescreve chaves existentes. A pasta e arquivos PEM são ignorados pelo Git e pelo contexto Docker. As chaves não são necessárias ao hash de senha e ainda não são utilizadas pelo back-end; ficam preparadas para uma futura autenticação por tokens. Não são copiadas para a imagem Docker.

## Endpoints de autenticação

1. `GET /login/csrf`: retorna `token` e `headerName`; preserve o cookie de sessão.
2. `POST /login/validar-login`: JSON com `login`, `senha` e `perfil` (`administrador` ou `professor`), incluindo o token CSRF no header retornado. Responde com `id`, `nome`, `login` e `perfil`. Credenciais incorretas, conta inexistente e conta inativa retornam 401 com a mesma mensagem.
3. `GET /login/me`: retorna os dados do usuário autenticado usando o cookie de sessão.
4. `POST /login/logout`: encerra a sessão (204). Requer CSRF; obtenha um token novo depois do login, pois o anterior é invalidado.

As senhas e hashes não são retornados pela API. A sessão é renovada ao autenticar. As chaves PEM continuam reservadas para uma eventual mudança para tokens; a implementação atual usa sessão HTTP.
## Duração da sessão

O cookie JSESSIONID é um cookie de sessão, sem Max-Age ou Expires, com HttpOnly e SameSite=Lax. Não há remember-me. O servidor não persiste sessões entre reinicializações. O logout invalida a sessão e remove o cookie.

O encerramento normal da sessão do navegador remove esse tipo de cookie, mas alguns navegadores restauram cookies de sessão ao recuperar abas. O fechamento do navegador não é um evento confiável para invalidar imediatamente a sessão no servidor; não há garantia universal de remoção nesse momento.

## Expiração por perfil e refresh

A sessão tem prazo fixo a partir do login: 8 horas para aluno, professor e administrador. Requisições comuns não estendem esse limite. O timeout de inatividade do container recebe o mesmo período de cada perfil.

`POST /login/refresh`, com o cookie da sessão e o header CSRF atual, concede mais 8 horas a partir da renovação. Está disponível apenas para professor e administrador; aluno recebe 403. Não restaura uma sessão expirada: é necessário entrar novamente (401). Não há renovação automática no front-end nesta etapa.

O cookie continua sem Expires ou Max-Age, para não persistir entre sessões do navegador. Professores usam a mesma política de sessão administrativa. A autenticação de alunos ainda não está implementada.
## Organização das configurações

`config/ClockConfig` fornece o relógio UTC, `CorsConfig` define as origens autorizadas, `PasswordConfig` define o hash e `SecurityConfig` conecta as regras de autenticação e CSRF. `SessionExpirationFilter`, também em `config`, verifica o prazo fixo das sessões.

Para acesso direto à API a partir de outra origem, configure `CORS_ALLOWED_ORIGINS` com a lista de origens permitidas, separadas por vírgula. O padrão permite `http://localhost:4200` e `http://127.0.0.1:4200`. Padrões também são suportados, conforme `allowedOriginPatterns` do Spring. As requisições cross-origin com cookie precisam enviar credenciais. O proxy `/api` do front-end continua sendo utilizado normalmente.

## Continuidade da sessão

As sessões são persistidas pelo Spring Session JDBC no MySQL, nas tabelas técnicas sessoes e sessoes_ATTRIBUTES. A migração database/migrations/003_persistir_sessoes.sql deve ser executada antes da atualização em um banco existente; init.sql inclui o esquema para bancos novos. O sufixo _ATTRIBUTES é definido pelo framework.

Reiniciar ou recriar o container do back-end não invalida uma sessão ainda válida, desde que o mesmo banco seja preservado. O cookie continua JSESSIONID, HttpOnly, SameSite=Lax e sem Max-Age/Expires; não há armazenamento de credenciais no navegador. A expiração absoluta por perfil, refresh, logout e bloqueio de usuário inativo permanecem ativos.

Erros 404 de recursos não invalidam a sessão. Se a consulta de autenticação falhar por conexão, 404 ou indisponibilidade, o front-end mostra /indisponivel e permite tentar novamente na rota de origem. Somente uma resposta 401 confirma que é necessário entrar novamente. Sessões anteriores, que existiam apenas em memória, exigem novo login uma vez após esta migração.

## Validação de requests

Todos os corpos de requisição usam @Valid nos controllers e constraints Jakarta Bean Validation nos records de model/request, com mensagens em português. Login e nomes são normalizados antes da validação; senhas não são aparadas. A senha opcional na edição aceita null ou string vazia para preservar a senha existente. IDs devem ser positivos.

GlobalExceptionHandler transforma violações, tipos incompatíveis e JSON inválido em HTTP 400. A resposta inclui mensagem (compatível com as telas atuais) e erros, uma lista de objetos campo/mensagem. Valores rejeitados, senhas e stack traces não são enviados. JSON malformado é identificado como request; erros de conversão identificam o campo quando o parser fornece seu caminho.

Exemplo: {"mensagem":"Verifique os campos informados. nome: O nome da matéria é obrigatório.","erros":[{"campo":"nome","mensagem":"O nome da matéria é obrigatório."}]}

## Documentação das services

Toda service nova ou alterada deve conter Javadoc acima dos métodos Java (JSDoc no TypeScript), descrevendo o contrato, os parâmetros, o retorno e as exceções relevantes. Dentro das funções, use comentários em bloco (/* ... */), em português, junto aos grupos de instruções que aplicam as regras de negócio e explicando seus motivos. Registre condições de aceitação e recusa, efeitos no cadastro ou na sessão, motivos das decisões e exceções importantes. Atualize esses comentários quando a regra mudar.

Priorize a intenção da regra, sem narrar cada linha, repetir a assinatura dos métodos ou documentar detalhes óbvios de implementação. Services de infraestrutura devem esclarecer o contrato com os fluxos de negócio, sem atribuir a elas decisões que pertencem às páginas, controllers ou outras services.

## Turmas

Cadastro administrativo em POST /turmas e listagem em GET /turmas. A identificação única é ensino + ano + letra: ensino F (Fundamental) ou M (Médio), ano inteiro de 1 a 9 para ambos e uma letra de A a Z. O request normaliza espaços e caixa das letras. Duplicidade retorna 409; dados inválidos retornam 400 com os erros por campo. A migração 004 cria a tabela; a migração 007 converte os valores existentes para F/M e reduz a coluna ensino a CHAR(1). API e banco usam os códigos; os nomes completos são apresentados somente na tela. A conversão preserva os IDs e vínculos das turmas.

### Professores

O cadastro em **Cadastros → Usuários → Professor** recebe nome, login, senha e pelo menos uma matéria. O status não faz parte do request de criação: o INSERT omite a coluna e o banco aplica ativo por padrão. Cada matéria tem sua própria lista de turmas, que pode estar vazia. A ativação/desativação é feita posteriormente pela lista. Matérias e turmas são escolhidas por chips com busca, sem aceitar texto livre como cadastro.

- POST /professores: corpo com nome, login, senha e materias: [{ materiaId: 1, turmas: [1, 2] }, { materiaId: 2, turmas: [3] }].
- GET /professores: retorna matérias com suas turmas específicas e o array agregado de turmas para compatibilidade de leitura.
- PATCH /professores/{id}/status: ativo false libera as reservas; true tenta reservar todas as combinações novamente.

Os endpoints exigem administrador. A senha é armazenada como hash e nunca devolvida. IDs inexistentes, matérias repetidas e turmas repetidas dentro de uma matéria são recusados. Um único professor ativo pode ministrar uma matéria em uma turma; professores inativos podem manter os mesmos vínculos. Um conflito de cadastro ou reativação retorna 409, sem criação parcial ou alteração parcial de status/vínculos.

A tabela professores_materias guarda as matérias do professor; professores_materias_turmas guarda suas turmas. A coluna ativo do vínculo é mantida junto com o status do professor na mesma transação. A coluna gerada reserva_ativa vale 1 para ativo e NULL para inativo. O índice único em matéria, turma e reserva_ativa impede colisões inclusive em requisições simultâneas, enquanto permite múltiplos vínculos inativos. A reativação bloqueia a conta durante a mudança e depende da disponibilidade de todas as reservas.

Bancos existentes devem executar as migrações 005 e 006, nessa ordem. O init.sql contém as tabelas para novas instalações. Os vínculos antigos de professores_turmas permanecem preservados e são identificados como anteriores na lista; nenhuma matéria é deduzida ou atribuída automaticamente a esses registros.

O componente visual-chip-select do design system tem busca sem distinção de acentos/caixa, seleção por clique ou Enter, navegação por setas, remoção de chips, limpeza pelo formulário e estado desabilitado. Há um exemplo no Storybook.

### Login e dashboard do professor

O perfil selecionado determina o repository de autenticação, sem fallback entre contas administrativas e professores. A sessão recebe ROLE_PROFESSOR ou ROLE_ADMINISTRADOR conforme a conta autenticada; a revalidação de status consulta o mesmo perfil. Requests antigos sem perfil continuam assumindo administrador por compatibilidade.

GET /professor/dashboard exige ROLE_PROFESSOR e usa apenas o ID do principal da sessão para retornar o cadastro público, matérias e turmas do próprio professor. O endpoint não aceita um ID escolhido pelo navegador. Professores não acessam endpoints de cadastros administrativos ou o catálogo Storybook.

### Gravação de questões

Aplicar database/migrations/008_criar_questoes.sql nos bancos existentes. POST /questoes exige professor autenticado e CSRF. O request contém materiaId, turmaId, tipo (objetiva/discursiva), peso, enunciado HTML, alternativas (mapa A–E; vazio na discursiva) e respostaCorreta (A–E na objetiva; null na discursiva). O autor é sempre extraído da sessão e a matéria/turma precisa pertencer a um vínculo ativo desse professor. A gravação retorna 201 com ID e conteúdo confirmado. GET /questoes/{id} permite consultar somente questões do próprio autor; IDs alheios retornam 404.

Enunciado e alternativas usam LONGTEXT para preservar imagens locais incorporadas. O Nginx aceita até 48 MB em POST /api/questoes; os campos HTML têm limite de 8 milhões de caracteres individualmente. O cadastro é transacional. As validações básicas de conteúdo e permissão não implementam regras de sorteio, distribuição de pesos ou publicação de provas. Os accordions do front-end não são recarregados ao atualizar a página; os registros no banco permanecem salvos.

GET /questoes lista apenas questões do autor da sessão, da mais recente para a mais antiga. A resposta é resumida (id, matéria, ensino, ano, letra, tipo, peso e resumo em texto), sem imagens ou alternativas completas. O conteúdo completo continua em GET /questoes/{id}, com a mesma restrição de autor.

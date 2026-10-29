# ESFINORA

Projeto novo com front-end Angular, back-end Spring Boot e MySQL. A tela de login ainda não está integrada ao back-end.

## Executar com Docker

Na pasta que contém este arquivo, com Docker e Docker Compose disponíveis:

```powershell
Copy-Item .env.example .env
# Edite as senhas no .env antes de iniciar.
docker compose up --build -d
```

- Front-end e login: http://localhost:4200/login
- Back-end: http://localhost:8080 (mantém a segurança padrão do Spring; não há endpoints de negócio nesta etapa).
- MySQL: disponível somente na rede interna, como `banco:3306`.

O Compose aguarda o banco aceitar conexões antes de iniciar o back-end. O volume `dados-banco` preserva os dados ao recriar os containers. As variáveis de inicialização do MySQL só são aplicadas quando o volume está vazio; mudar as senhas no `.env` não altera usuários de um banco já inicializado.

```powershell
docker compose logs -f
docker compose down
```

`down` encerra os serviços e mantém o banco. Para atualizar as imagens das aplicações após mudanças, execute novamente `docker compose up --build -d`.

## Backup automático do banco

O Compose inclui o serviço `backup`, que gera `database/backups/dump.sql` no
servidor ao iniciar e a cada quatro horas. Mantém somente o último dump
concluído: o anterior só é substituído depois que o novo termina sem erro,
com conteúdo e marcador de conclusão. Consulte [operação e restauração](database/backup/README.md)
para mudar o destino, executar manualmente ou verificar o serviço.

## Imagens das aplicações

Cada aplicação possui seu próprio Dockerfile e .dockerignore. O front-end compila com Node 24 e serve os arquivos com Nginx, incluindo acesso direto às rotas Angular. O back-end compila com Maven Wrapper e Java 27, conforme o pom.xml, e executa como usuário sem privilégios. O empacotamento da imagem não executa os testes que dependem do contexto do banco.

A configuração prepara a infraestrutura; não cria tabelas, endpoints nem integra a autenticação.

### Abrangência das questões

A matéria é obrigatória. O professor seleciona um ensino e ano em que ministra essa matéria e seleciona suas turmas vinculadas nessa matéria e ano por chips. O campo de turmas é opcional: vazio significa o ano inteiro. Uma questão é gravada uma única vez: turmas específicas ficam em `questoes_turmas`; a abrangência `ano` inclui dinamicamente todas as turmas com vínculo ativo do professor na matéria e ensino/ano. A consulta continua restrita ao autor. Bancos existentes precisam executar `database/migrations/009_abrangencia_questoes.sql`, que preserva o vínculo das questões anteriores.

### Edição e preservação de questões

A lista consulta somente a questão atual em `questoes`, sem exibir históricos. `PUT /questoes/{id}` recebe `{ versaoId, revisao, questao }`. Enquanto a versão atual não estiver usada numa prova respondida, ela é atualizada no mesmo registro. A revisão aumenta para impedir sobrescritas por formulários desatualizados (409), sem criar uma versão de negócio.

Se `questoes_versoes.utilizada_em_prova_respondida` for verdadeiro, a edição preserva essa versão e cria a próxima. Uma versão nova ainda não utilizada poderá ser editada no mesmo registro. Bancos existentes precisam aplicar `011_versionamento_condicional_questoes.sql`.

**Integração futura com provas:** o módulo ainda não existe. Ao registrar uma prova respondida, deverá referenciar `questoes_versoes.id` por chave estrangeira e marcar essa versão como utilizada na mesma transação, bloqueando primeiro a linha pai de `questoes` com FOR UPDATE (a mesma ordem de bloqueio da edição). Exibição e correção da prova devem ler o conteúdo, gabarito e peso da versão referenciada. Não há endpoint para o professor alterar a marca de uso. Versões utilizadas não podem ser atualizadas ou removidas pela aplicação.

### Pendências de questões no dashboard

`GET /professor/dashboard/pendencias-questoes` calcula para o professor da sessão, em cada vínculo ativo de matéria/turma, os mínimos: 4 questões de peso 0,5; 3 de 1,0; 2 de 1,5; 1 de 2,0. A API retorna combinações incompletas e o déficit por peso. O dashboard soma esses déficits em um card de indicador com o total faltante e link para a lista de questões. Questões objetivas e discursivas contam igualmente. Questões do ano inteiro contam para cada turma correspondente; questões específicas contam somente nos vínculos selecionados. Apenas registros atuais do próprio autor são contados, sem somar versões históricas.

### Consulta de alunos e paginação

O professor acessa `Alunos → Listar` e consulta somente alunos de turmas com vínculo ativo em suas matérias. `GET /professor/alunos` aceita `pagina` (iniciando em 1), `tamanho` (15, 25 ou 50) e `turmaId` opcional. A autorização é aplicada na contagem e na seleção no banco, sem duplicar alunos quando há várias matérias. A migração `012_alunos.sql` cria a estrutura de alunos (nome, CGM, status e turma); o cadastro administrativo foi acrescentado na migração 013; o login de alunos ainda não foi implementado.

Alunos e questões usam o componente `visual-pagination`, documentado no Storybook, com dropdown de 15/25/50, intervalo de registros e navegação. Alunos são paginados no servidor; questões paginam os resumos já carregados e filtrados na tela. Mudanças nos filtros ou tamanho retornam à primeira página.

As pendências e as opções de cadastro/pesquisa usam somente turmas vinculadas ativamente ao professor na matéria. Ser professor da turma A não oferece a turma B só por pertencer ao mesmo ano. Turmas vazias no cadastro continuam significando todas as suas turmas daquela matéria/ano. A API recusa turma específica sem vínculo, inclusive em requisição manual. A consulta de alunos também permanece limitada às turmas do professor.

### Submenus laterais

A navegação administrativa e o submenu Alunos do professor usam `visual-navigation-menu`, documentado no Storybook. Cada nível abre um painel lateral ancorado ao item, com fallback de posição para caber na tela. O componente usa o overlay do Angular CDK para não ser cortado pela rolagem da barra lateral. Fecha ao navegar, clicar fora ou pressionar Esc. Setas direcionais, Home e End também permitem navegação pelo teclado.

As tabelas de questões e alunos usam `visual-table-scroll`: quando há conteúdo fora da largura disponível, uma barra sincronizada permanece na parte inferior visível enquanto a tabela é percorrida. O controle permite arrastar, usar as setas laterais ou navegar por teclado. A barra desaparece quando todas as colunas cabem na tela. O componente também está no Storybook.

### Cadastro administrativo de alunos

Somente administradores podem cadastrar em `Cadastros → Usuários → Cadastrar → Aluno` (`POST /alunos`) e consultar a lista geral paginada (`GET /alunos`). O cadastro exige nome, CGM numérico textual (preserva zeros iniciais), turma e status escolar: `M` Matriculado, `T` Transferido, `R` Remanejado ou `D` Desistente. Número é um inteiro positivo opcional. O formulário oferece Limpar, Cadastrar e Cadastrar e continuar, sem persistir dados no navegador. CGM duplicado retorna 409. Professores não podem cadastrar alunos: permanecem limitados à leitura de suas turmas em `/professor/alunos`.

A migração `013_cadastro_alunos.sql` acrescenta número e status M/T, converte o status anterior e remove o booleano `ativo` de alunos. Cadastro/login de aluno não são a mesma operação; o login de alunos permanece fora desta implementação.

### Importação de alunos por CSV

Em `Cadastros → Usuários → Cadastrar → Aluno`, selecione a aba **Importar CSV**. Baixe o modelo, preencha o arquivo em UTF-8 e use **Validar CSV** antes de **Importar alunos**. Cabeçalho: `nome;numero;cgm;status;ensino;ano;letra`. A coluna número pode estar ausente ou vazia; CGM é textual para preservar zeros. Status aceita M/T (ou Matriculado/Transferido), ensino F/M (ou Fundamental/Médio), ano 1–9 e letra A–Z; a turma precisa estar cadastrada. Vírgula e ponto e vírgula, aspas escapadas, UTF-8 BOM e quebras CRLF são aceitos. Até 1.000 alunos e 512 KB por arquivo.

`POST /alunos/importar/validar` retorna prévia e erros por linha/campo sem gravar. `POST /alunos/importar` revalida o mesmo conteúdo e grava o lote inteiro na mesma transação, ou não grava ninguém quando há erros. CGMs repetidos no arquivo ou existentes são recusados; cadastros existentes não são atualizados. As duas operações são exclusivas do administrador. Nenhum arquivo é armazenado no servidor ou no storage do navegador.

### Liberações de provas por turma

No menu **Provas**, a aba **Liberações por turma** permite selecionar uma turma e registrar título, matéria e prazo. Cada operação cria uma única liberação para a turma. A aba **Consulta por aluno** é somente de consulta: mostra as liberações do professor para a turma atual do aluno. A confirmação pelo aluno inicia o sorteio e a realização descritos abaixo.

O vínculo ativo do professor com a turma e a matéria é validado no servidor. A situação usa instantes UTC, com início inclusivo e fim exclusivo. A lista aberta é atualizada a cada 30 segundos.

Em bancos existentes, aplicar as migrações 014, 015 e 016 nessa ordem. A migração 016 remove o vínculo individual e preserva as liberações existentes pela turma já registrada. Bases novas usam o init.sql atualizado.

- GET /professor/provas/turmas/{turmaId}: liberações próprias da turma.
- POST /professor/provas/turmas/{turmaId}/liberacoes: liberação coletiva com título, matéria e período.
- GET /professor/provas/alunos/{alunoId}: consulta das liberações da turma atual do aluno.
- As consultas aceitam pagina e tamanho (15, 25 ou 50). Não existe POST de liberação por aluno.
### Alunos por ano letivo

O cadastro e as consultas de alunos de 2026 utilizam a tabela alunos_2026, inclusive importação CSV e liberação de provas. Em bancos existentes, aplicar database/migrations/015_alunos_2026.sql uma vez após a migração 014; o RENAME preserva os registros e as referências das liberações. O init.sql já cria a tabela com o novo nome para bases novas. A massa opcional database/seeds/alunos-2026.sql cadastra três alunos fictícios por turma sem duplicar CGMs em reexecuções.

### Acesso e provas do aluno

O perfil Aluno autentica por CGM em alunos_2026, sem senha. Somente matriculados (M) podem entrar; a sessão dura oito horas e não admite refresh. O filtro de sessão revalida matrícula e CGM nas requisições. No POST /login/validar-login, enviar login com o CGM e perfil aluno; senha é dispensada apenas nesse perfil. Professores e administradores continuam exigindo senha.

Após entrar, o aluno abre /aluno/provas, também disponível como Provas no menu. GET /aluno/provas usa exclusivamente a identidade da sessão e retorna liberações da turma atual dentro do período, com vínculo ativo do professor e da matéria. Não retorna questões nem gabaritos. Liberações futuras e encerradas não aparecem. A consulta tem paginação 15/25/50 e atualização a cada 30 segundos.

A barra compara provas entregues com o total de provas da lista, incluindo tentativas em andamento e entregues. O total independe da página consultada. O componente visual-progress pertence ao design system e está demonstrado no Storybook.

### Realização de provas e Redis

Aplicar database/migrations/018_execucao_provas.sql após a migração 017 em bancos existentes. O init.sql inclui a estrutura para bases novas. O Compose agora inclui Redis em rede interna, com volume dados-redis, AOF habilitado, appendfsync always e política noeviction. O back-end aguarda sua verificação de saúde.

O professor define duração de 1 a 480 minutos e token de início ao liberar por turma. Editar permite substituir o token (em branco, mantém o atual) e alterar a configuração para novos inícios. O início é validado por hash. Uma cópia cifrada permite consultar o token somente pelo professor autor, no endpoint de edição com Cache-Control no-store. Liberações antigas precisam receber token antes do primeiro início. Tentativas já iniciadas preservam conteúdo, ordem e prazo.

O aluno abre Iniciar prova, lê as regras, aceita e informa o token. O servidor sorteia exclusivamente questões do professor e da matéria, no mesmo ensino/ano e com abrangência de ano ou vínculo com a turma. A composição é 4 de peso 0,5; 3 de 1,0; 2 de 1,5; e 1 de 2,0. A primeira tem peso 0,5 e a de 2,0 fica aleatoriamente da sétima à décima posição. Falta de questões de qualquer peso impede a criação parcial da tentativa.

A cópia das dez questões, alternativas e ordem é salva no MySQL uma única vez por aluno/liberação. O início concorrente é coordenado por chave Redis com proprietário e expiração; a chave única no MySQL também impede duplicação. Não são usados bloqueios de aluno nem de questões para cada marcação.

As respostas e revisões são mantidas em hash Redis separado do conteúdo fixo. Scripts Lua verificam prazo e revisão e gravam atomicamente, evitando sobrescritas entre abas. O prazo é o menor entre início + duração e fim da liberação; fechar o navegador não pausa o relógio. O front-end indica salvamento pendente e aguarda salvar antes de navegar. Campos não são armazenados em localStorage ou sessionStorage.

Na entrega, o Redis sela as respostas antes da consolidação. Uma transação no MySQL persiste as dez respostas, a entrega e a pontuação interna; questões em branco recebem zero e discursivas respondidas aguardam correção. Falha na consolidação mantém o Redis selado para repetir a operação sem perda. Provas entregues são conservadas no MySQL; o cache Redis pode expirar sete dias depois.

O cronômetro entrega automaticamente ao zerar. Uma rotina a cada 15 segundos e a consulta ao retornar também encerram provas vencidas com navegador fechado. Envio antecipado incompleto exige confirmação. A entrega encerra a sessão; após novo login, Ver respostas mostra somente o conteúdo da prova e o que o aluno respondeu, sem nota nem gabarito.

Endpoints do aluno (a identidade sempre vem da sessão):

- GET /aluno/provas/{id}/regras: regras e configuração pública.
- POST /aluno/provas/{id}/iniciar: token e aceite, com sorteio idempotente.
- GET /aluno/provas/{id}/tentativa: retomar ou consultar entrega.
- PUT /aluno/provas/{id}/questoes/{numero}/resposta: resposta e revisão.
- POST /aluno/provas/{id}/submeter: confirmação de envio incompleto.
- PUT /professor/provas/turmas/{turmaId}/liberacoes/{id}: editar configuração da liberação.

### Consulta e edição de liberações

A consulta por turma mostra inicialmente apenas a lista. Nova liberação abre o cadastro; Editar faz GET /professor/provas/turmas/{turmaId}/liberacoes/{id} e preenche os dados atuais antes de abrir o formulário. Cancelar descarta os campos sem fechar a consulta. Datas e horários usam visual-date-field, compartilhado com o Storybook e configurado em português; o modelo usa ISO para preservar o instante UTC e o controle mostra o horário local.

Aplicar database/migrations/019_token_consultavel.sql em bancos existentes. O token continua sendo validado por hash; sua cópia consultável é cifrada com AES-GCM e chave AES protegida por RSA-OAEP. Somente o autor pode consultar os detalhes. Listas, endpoints do aluno e logs não retornam o token em texto.

O Compose monta back-end/keys em modo de leitura. As chaves publica.pem e privada.pem permanecem fora do Git e da imagem, conforme .gitignore e .dockerignore. Preserve esse par de chaves junto ao backup: substituí-lo impede recuperar tokens cifrados anteriormente.

Tokens gravados antes dessa migração existiam somente como hash e não podem ser recuperados. A edição identifica esses casos e exige informar um token e salvar uma vez. A alteração não modifica tentativas já iniciadas. A consulta passa então a preencher o token normalmente.

### Confirmação de entrega pelo aluno

Toda entrega manual apresenta confirmação Sim/Não, inclusive quando todas as questões foram respondidas. Havendo questões em branco, o mesmo modal informa quantas faltam e que ficarão sem ponto. A confirmação do servidor abre o aviso de entrega com a orientação para aguardar a correção do professor. A página permanece nesse aviso até OK, botão de fechar ou Escape; somente então segue para o login. A entrega automática por prazo também apresenta o aviso. Consultar uma prova já entregue após novo login não reapresenta o aviso.

### Correção e feedback do professor

Aplicar database/migrations/020_correcao_questoes.sql em bases existentes. A migração preserva as respostas e amplia a pontuação para duas casas decimais, acrescentando feedback, revisão de correção, data e professor responsável. O init.sql já inclui esses campos.

No menu Provas, a aba Correções lista somente provas entregues de autoria do professor autenticado. Há filtros por turma e situação (todas, com discursivas pendentes, corrigidas), com paginação 15/25/50. Corrigir ou Revisar abre o conteúdo fixo da prova, as respostas originais e os formulários por questão.

O professor pode atribuir pontuação entre zero e o peso das discursivas respondidas e salvar feedback de até 3.000 caracteres em qualquer questão. A nota das objetivas permanece automática, e questões em branco continuam com zero. A nota parcial e a quantidade de discursivas pendentes são atualizadas após salvar. Quando não há discursiva respondida sem pontuação, a prova aparece como corrigida.

A revisão otimista impede sobrescritas silenciosas entre abas. O botão único Salvar correção ao final envia todas as notas e feedbacks. A validação completa antecede a gravação em uma única transação; erros ou conflitos cancelam toda a operação. Sair ou recarregar descarta os rascunhos não salvos. Conteúdo e resposta do aluno não são alterados durante a correção.

Notas, feedback e gabarito permanecem restritos ao professor: a publicação para o aluno será implementada em uma etapa futura. A correção usa o MySQL consolidado da entrega, sem alterar o estado de respostas do aluno no Redis.

- GET /professor/correcoes?pagina=1&tamanho=15&situacao=todas: entregas próprias, com turmaId opcional.
- GET /professor/correcoes/{id}: cópia entregue, nota parcial e questões para correção.
- PUT /professor/correcoes/{id}: questoes, com numero e correcao (pontuacao, feedback e revisao) de todas as questões.

### Relatórios e exportação

O menu Relatórios está disponível para administradores e professores. A lista de alunos usa a turma selecionada; os resultados usam turma e liberação de prova. O administrador consulta a escola e o professor consulta seus vínculos ativos e suas próprias provas. Alunos não acessam estes endpoints.

Escolha os campos que devem aparecer na tabela e o formato de saída. PDF / Impressão abre o diálogo do navegador, onde é possível selecionar Salvar como PDF. O documento remove a navegação do portal, repete cabeçalhos nas páginas e inclui toda a turma. CSV baixa os mesmos campos e linhas, com separador ponto e vírgula, aspas escapadas e UTF-8 com BOM. Valores que poderiam ser interpretados como fórmulas recebem apóstrofo de proteção.

Notas de entregas com discursivas pendentes são identificadas como parciais, inclusive quando apenas a coluna de nota é selecionada. Provas não iniciadas ou em andamento não apresentam nota. Tentativas históricas permanecem na relação mesmo após mudança de turma. Consultas e preferências ficam em memória; não são persistidas pelo formulário.

Endpoints GET, com respostas no-store: /relatorios/turmas, /relatorios/alunos?turmaId=, /relatorios/provas?turmaId= e /relatorios/resultados?provaId=. Identificadores devem ser positivos; recursos fora do escopo retornam 404.

### Indicadores do dashboard

Administradores e professores consultam GET /dashboard/indicadores (no-store); alunos não têm acesso. Considera provas cujo início da disponibilidade já ocorreu e alunos atualmente matriculados na turma. O professor consulta apenas suas liberações com vínculo ativo na matéria/turma; a administração abrange todas. Liberações futuras são excluídas; provas cujo prazo terminou continuam na contagem de pendências.

Os cards contam alunos distintos: entregou alguma prova e possui alguma prova ainda não entregue. Um aluno pode participar dos dois grupos. Média geral e barras por turma usam apenas tentativas entregues sem discursivas aguardando nota, com peso igual por tentativa corrigida. Ausência de média permanece nula, exibida como travessão. O donut conta avaliações por aluno (não iniciada, em andamento, aguardando correção e corrigida).

O componente visual-chart do design system aceita doughnut e barras com cores distintas por categoria, escala máxima configurável e valores nulos, com tabela acessível alternativa. Exemplos de médias e situação das provas foram adicionados ao Storybook.

### Perfil do professor e massa de demonstração

O quarto card do dashboard do professor (questões faltantes) integra a mesma grade dos indicadores de provas. As matérias e turmas passaram para Meu perfil, rota /perfil, exclusiva do professor e somente para consulta.

A massa local está documentada em [database/seeds/README.md](database/seeds/README.md), com logins, CGMs, token, cenários e instruções para retomar a carga pelas APIs. O script não é executado automaticamente no início do sistema. Backups e estado local não são versionados.

### Acesso pela rede local

O frontend publica a porta `${PORTA_FRONTEND:-4200}` em `${HOST_FRONTEND:-0.0.0.0}`. Em outra máquina da mesma rede, acesse `http://IP-DO-SERVIDOR:4200`; `0.0.0.0` é o endereço de escuta, não o endereço para abrir no navegador. Para limitar novamente ao computador local, defina `HOST_FRONTEND=127.0.0.1` no .env e recrie o frontend com `docker compose up -d front-end`.

O navegador usa `/api` no mesmo endereço do frontend. O Nginx encaminha ao backend pela rede Docker e preserva o Host com a porta original, permitindo validar corretamente a origem das requisições. Não é necessário publicar o MySQL ou o backend na rede para acessar o portal. Se o acesso remoto não chegar ao frontend, confira conectividade da rede e permissão de entrada TCP na porta 4200 no firewall do servidor.

### Interface Gradient Able

O frontend e o catálogo de componentes adotam uma interface inspirada no [Gradient Able React Free](https://codedthemes.com/demos/admin-templates/gradient-able/react/free/app/dashboard/analytics), implementada nos componentes Angular existentes. O tema usa Poppins local, cabeçalho em gradiente azul, navegação branca, cards em gradientes e superfícies com bordas discretas. Tokens e estilos globais ficam no design system; as regras de negócio, rotas, serviços, dados e permissões permanecem nos fluxos existentes.

O recibo de entrega do aluno também apresenta o progresso total das provas, incluindo a entrega atual, pelo componente visual-progress do design system. O backend envia total/concluídas na própria resposta de entrega antes de invalidar a sessão, tanto no envio manual quanto no automático. Não há consulta autenticada posterior ao logout. Falha na consulta do resumo mantém a entrega confirmada e o encerramento da sessão; o modal informa a indisponibilidade do progresso.

Provas já corrigidas continuam revisáveis pelo professor autor. A tela apresenta Revisar correção e Salvar alterações; notas discursivas e feedbacks podem ser atualizados, preservando o controle de revisão e o salvamento único. Objetivas e respostas em branco mantêm a pontuação automática. Alterar um campo remove a mensagem anterior de salvamento até a nova confirmação.

### Edição de alunos e professores

Na lista administrativa de Usuários, os perfis Professor e Aluno possuem ação Editar. As rotas /cadastro/professores/:id/editar e /cadastro/alunos/:id/editar exigem administrador. GET/PUT /professores/{id} e GET/PUT /alunos/{id} carregam e atualizam os cadastros. O professor continua com consulta dos próprios alunos e do perfil, sem autorização de edição cadastral.

Professor permite alterar nome, login, senha opcional e matérias com turmas por chips. Senha em branco conserva o hash; o status continua sendo alterado pela lista. A troca de vínculos é transacional e preserva a exclusividade de cada matéria/turma entre ativos. Conflitos desfazem também as mudanças de dados pessoais. Aluno permite nome, número opcional, CGM, situação M/T e turma; CGM permanece textual e único. Nenhuma edição recria identidades ou altera cópias e respostas de provas. Os formulários usam validação do design system e descartam dados ao sair.

### Estados, filtros e manutenção de turmas

Alunos aceitam M (Matriculado), T (Transferido), R (Remanejado) e D (Desistente), inclusive na importação CSV, que também aceita os nomes completos. Apenas matriculados continuam habilitados para login. Bancos já existentes devem executar uma vez `database/migrations/20261027-estados-alunos.sql`; a base deste ambiente já foi atualizada. Novos bancos usam a restrição atualizada de `database/init.sql`.

As listas administrativas de professores e alunos oferecem nome/login ou nome/CGM, status e turma. A consulta de alunos do professor mantém o escopo de suas turmas. A pesquisa de alunos é feita no banco antes da contagem e paginação.

Administradores podem editar turmas pela lista e excluir após confirmação. `GET/PUT/DELETE /turmas/{id}` validam o identificador; edição mantém o ID e rejeita combinações duplicadas de ensino/ano/letra. Exclusão bloqueia qualquer aluno ou professor vinculado, inclusive não matriculados e professores inativos. Questões e provas referenciadas também impedem remoção para preservar os dados.

### Ordenação das listas

Clique no cabeçalho de uma coluna para ordenar de forma ascendente; clique novamente para descendente. A seta indica a ordem ativa. Trocar a coluna inicia em ascendente. Disponível nas listas de administradores, professores, alunos, matérias, turmas, questões cadastradas/faltantes, provas, correções e relatórios. Campos de ações não ordenam. Relatórios exportam as linhas na ordem escolhida.

Listas paginadas pelo servidor enviam `ordenacao` e `direcao=ASC|DESC`, mantêm filtros e voltam à primeira página ao trocar a ordem. O backend resolve apenas colunas permitidas, aplica ORDER BY antes de LIMIT/OFFSET e usa ID para desempate. Número de aluno vazio fica ao final em ambos os sentidos. Questões e demais listas carregadas integralmente ordenam o conjunto filtrado antes de separar as páginas no frontend. Componentes compartilhados: `ListSort` e `SortHeaderComponent` no design system, com demonstração no Storybook.

### Alunos considerados nas estatísticas

Transferidos (T) e desistentes (D) não entram nos cards de participação, no total por situação das provas, nas médias gerais ou por turma e nos resumos dos relatórios. Matriculados (M) e remanejados (R) continuam considerados. O status atual é aplicado inclusive às provas já entregues. Listas cadastrais e relatórios preservam as linhas e o histórico, com aviso de que os alunos excluídos não participam dos totais.

### Configuração das provas por assunto e peso

O professor digita o peso de cada questão, positivo e com até duas casas decimais. Na liberação, informa o valor total, de 3 a 200 questões, os assuntos e os pesos permitidos em cada assunto. Todos os assuntos selecionados devem aparecer ao menos uma vez.

**Conferir distribuição** calcula automaticamente as quantidades por assunto/peso para atingir exatamente o valor e o número de questões. A análise usa centavos inteiros e minimiza as faltas de estoque. Quando uma combinação é possível mas faltam questões, mostra quanto cadastrar em cada grupo. Quando o valor não pode ser formado com aqueles pesos e aquela quantidade, informa incompatibilidade matemática. A liberação exige uma análise disponível e repete a conferência no servidor; a distribuição conferida é persistida e só as questões e a ordem são sorteadas por aluno.

Abaixo dos pesos de cada assunto, a opção **Garantir pelo menos uma questão de cada peso selecionado** ativa essa exigência apenas para aquele assunto, inclusive “Geral”. Ela começa desmarcada e é salva na configuração da liberação. Desmarcada, os pesos continuam apenas permitidos e podem não aparecer na distribuição. Alterar a opção exige conferir a distribuição novamente. Se os mínimos não couberem na quantidade ou no valor total, a análise informa incompatibilidade; se faltar estoque, informa as questões a cadastrar. Configurações antigas mantêm a regra desativada.

API: POST /professor/provas/turmas/{turmaId}/analisar recebe materiaId e configuracao, com valorTotal, quantidadeQuestoes e assuntos: [{assuntoId,pesos:[1.25,2.5]}]. A liberação/edição recebe os mesmos critérios e composicao: [{assuntoId,peso,quantidade}] devolvida pela análise. Não há mínimos fixos por peso nem posição reservada para pesos específicos. A primeira questão também participa do embaralhamento completo.

As faltas exibidas no dashboard e na lista de questões comparam as distribuições das provas já liberadas com o estoque atual. Sem uma liberação, não há quantidade mínima genérica a cadastrar. O banco de assuntos continua compartilhado por matéria, com sugestões e criação por chip. Cada questão possui um assunto específico obrigatório. “Geral” fica reservado à liberação: considera questões de qualquer assunto, respeitando professor, matéria, turma/ano, pesos, quantidade e valor total. É selecionado sozinho, sem combinação com assuntos específicos. A análise, o sorteio e as pendências usam a mesma abrangência.

Respostas, entrega, correção e revisão usam a quantidade real da cópia do aluno. Alterar uma liberação não modifica tentativas iniciadas. Notas da correção e dos relatórios permanecem no valor original; médias do dashboard convertem cada prova para 0 a 10 para comparar provas de valores diferentes.

A estrutura atual está em database/init.sql. Liberações anteriores sem os novos critérios precisam ser conferidas novamente pelo professor; tentativas iniciadas continuam disponíveis.

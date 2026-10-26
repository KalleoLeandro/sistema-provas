# Front-end do portal ESFINORA

ESFINORA — Sistema de Avaliação e Gestão Educacional.

Projeto novo em Angular. O portal inclui login, dashboards por perfil e cadastros administrativos.

- Aluno: CGM numérico.
- Professor e administrador: login e senha.
- Administrador e professor autenticam por cookie de sessão e entram em `/dashboard`, com painéis distintos. A autenticação de alunos ainda não está implementada.
- Campos apenas em memória, limpos ao enviar, trocar de perfil e sair da página. Autocomplete desativado; navegadores podem ignorar essa preferência.

`src/app/design-system` contém cores, espaçamentos e os componentes de campo, botão, cartão e aviso. Nomes de tabelas, entidades e termos do domínio usam português (aluno, professor, administrador). Termos de desenvolvimento seguem o padrão em inglês: config, security, components, services, repositories, Component e Directive. APIs do Angular e atributos HTML mantêm seus identificadores técnicos.

Use `npm start`, `npm run build` e `npm test -- --watch=false`.

## Cores da identidade visual

Referência: [Manual de Identidade Visual do Paraná Educação, página 5](https://www.paranaeducacao.pr.gov.br/sites/parana-educacao/arquivos_restritos/files/documento/2024-03/MANUAL%20DE%20IDENTIDADE%20VISUAL.pdf).

Os tokens institucionais ficam em `src/app/design-system/styles.css`: azul `#007A9F`, azul escuro `#265971`, verde lima `#C3D552`, verde claro `#98C455`, verde `#83BC56` e cinza `#CAD4E1`. A interface usa azul nas ações, azul escuro nos títulos e verdes como acentos decorativos. Tons neutros e fundos suaves são adaptações para a interface, sem classificação como cores oficiais. Mensagens de erro mantêm cores semânticas próprias.

## Navegação autenticada

`/` redireciona para `/dashboard`, página inicial administrativa dentro do main-layout. Usuários sem sessão válida vão para `/login`. O menu oferece Dashboard para administradores e professores, com navegação administrativa exclusiva do administrador. `/inicio` é apenas a página base do aluno, cuja autenticação ainda não está implementada.

As rotas restauram a sessão por `GET /api/login/me`. Login e logout usam CSRF, via proxy `/api` para o back-end. Cookies são geridos pelo navegador; credenciais e dados do formulário não são persistidos. O Nginx encaminha `/api/` no Docker, e `proxy.conf.json` faz o mesmo em `npm start`.
## Ações dos formulários de cadastro

`design-system/form-actions.ts` fornece Limpar, Cadastrar e Cadastrar e continuar. Limpar redefine o formulário e as mensagens sem enviar dados. Cadastrar salva e segue para a rota Listar do recurso. Cadastrar e continuar salva, limpa os campos e mantém a tela aberta para o próximo registro. Durante o envio, as três ações ficam desabilitadas para evitar duplicidade.

Aplicado aos formulários existentes de administrador e matéria. Os formulários de professor, aluno e turma ainda não estão implementados; deverão reutilizar o mesmo componente. As páginas de listagem ainda são páginas-base.

## Design system / Storybook

O build Docker do front-end gera o portal e o catálogo estático. Entre como Master User e acesse `/design-system` diretamente. O Storybook ocupa a página inteira, fora do main-layout e sem link no menu.

Na raiz do projeto, execute `docker compose up -d --build`. O Storybook usa o mesmo container e a mesma porta do front-end; não existe serviço ou porta Docker exclusiva para ele.

O Nginx protege `/storybook/` e todos os seus arquivos por `auth_request`, consultando `GET /design-system/acesso` no back-end com o cookie da sessão. O servidor exige uma conta administrativa ativa com `master = TRUE`, verificada pelo ID da sessão. Visitantes recebem 401 e administradores comuns recebem 403. Os arquivos são servidos com `Cache-Control: no-store`. A rota independente mantém os guards de autenticação e de acesso master.

Para desenvolver os componentes localmente, `npm run storybook` inicia o servidor de desenvolvimento em http://127.0.0.1:6006, restrito à máquina local e sem a autenticação do Nginx. Esse comando não é usado na implantação. `npm run build-storybook` gera `dist/storybook`. As histórias usam os componentes reais de `src/app/design-system`.

O dashboard renderiza componentes separados para administrador e professor. O painel do professor consulta GET /api/professor/dashboard e exibe somente suas matérias e turmas, identificadas pela sessão no servidor. Tentativas de acessar rotas administrativas são redirecionadas ao dashboard, e as APIs administrativas permanecem bloqueadas por perfil.

## Interface de cadastro de questões

O professor acessa /questoes/cadastrar pelo menu Cadastrar questões. A interface usa como referência pages/exam-create do projeto antigo: matéria/turma, cadastro sequencial de questões, tipo objetiva ou discursiva, peso, enunciado rico, alternativas A–E, resposta correta, imagens e prévia. Os vínculos são lidos do endpoint já existente /professor/dashboard.

Cadastrar envia POST /questoes e retorna para /questoes/listar após a confirmação. Cadastrar e continuar envia a mesma gravação, acrescenta a questão recolhida em um accordion acima do formulário e abre um novo editor com a matéria/turma preservada. Falhas mantêm o rascunho e não criam accordion. Limpar afeta somente o formulário atual. A coleção visual existe apenas na memória desta visita: sair, fechar ou atualizar descarta essa coleção, sem apagar as questões do banco. A página não carrega os cadastros anteriores.

Imagens locais são gravadas junto com o HTML da questão. O banco armazena autor da sessão, matéria, turma, tipo, peso, enunciado, alternativas e resposta correta. Não foram implementados sorteio, cotas de pesos, publicação automática ou cadastro de provas.

O editor e a prévia reutilizáveis ficam no design system. A prévia usa a sanitização padrão do Angular e não apresenta o gabarito ao lado das alternativas.

A lista em /questoes/listar consulta os resumos persistidos do próprio professor, com matéria, turma, tipo e peso. Visualizar busca o conteúdo completo por ID somente quando solicitado. O menu oferece Cadastrar questões e Listar questões.

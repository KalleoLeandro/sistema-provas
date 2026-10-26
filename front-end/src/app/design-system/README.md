# Design system ESFINORA

Catálogo Storybook dos componentes reutilizáveis do portal, incluindo os itens identificáveis nas imagens de referência. Os exemplos usam dados fictícios e não chamam a API.

## Executar

No front-end:

- npm run storybook: desenvolvimento local em http://127.0.0.1:6006, sem autenticação; não usar para publicar o catálogo.
- npm run build-storybook: gera dist/storybook.
- npx tsc --noEmit -p .storybook/tsconfig.json: verifica os tipos do catálogo.

Na raiz do projeto:

- docker compose up -d --build: inclui o catálogo no container do front-end.
- Acesso direto em `/design-system`, exclusivo do Master User, fora do main-layout e sem link no menu.
- Todos os arquivos sob `/storybook/` exigem autorização da sessão no back-end, inclusive acessos diretos a scripts, estilos, índice e iframe.

## Organização

- tokens.css: cores, fontes, alturas, espaços e sombras.
- components.ts: campoVisual (input, select e textarea), botaoVisual (button e a), visual-campo, visual-cartao e visual-aviso.
- styles.css: estilos básicos compartilhados pelo portal.
- catalog.css: estilos adicionais NG-ZORRO e Quill.
- providers.ts: idioma pt-BR, ícones locais e tema derivado de --cor-primaria.
- library.ts: módulos de base e extensões disponíveis para uso nas páginas.
- extensions/: componentes próprios, incluindo campos de tags e senha, gráficos, upload local, PDF, Markdown, editor, rolagem incremental e composições de layout.
- stories/: exemplos funcionais, estados e documentação.
- catalog.json: inventário dos componentes e das histórias.

## Usar nas novas telas

Importe somente os módulos ou componentes necessários de library.ts (ou diretamente de extensions/). Os componentes existentes continuam em components.ts.

Para ativar os controles de base nas páginas do portal, acrescente provideDesignSystem() aos providers do app.config.ts e importe catalog.css antes de styles.css na entrada global de estilos. O Storybook já faz essa configuração. Os estilos adicionais estão separados para não aumentar o carregamento das telas que ainda usam apenas os componentes básicos.

Os controles NG-ZORRO expõem a API original (nz-*, ngModel e reactive forms). A biblioteca de base usa licença MIT. As extensões de tags, senha e editor implementam ControlValueAccessor. Os nomes de perfis, rótulos e conteúdos de domínio são em português.

### Padrões existentes

- Botões: botaoVisual, variant=primary|secondary|edit|danger|success|continue; size=compact para listas.
- Campos: campoVisual dentro de visual-campo com rotulo e identificador.
- Cadastros pequenos: form-page no contêiner do título e cartão, com largura máxima de 640px e centralização horizontal responsiva.
- Formulários: form-visual; ações form-actions e form-actions-end; cadastro múltiplo visual-form-actions.
- Feedback: visual-aviso para erros; form-feedback com role=status para sucesso.
- Cards: visual-cartao com density=compact para listas.
- Tabelas: tabela-visual, status-visual e acoes-registro. Use RecordNameDirective com nomeRegistro em th scope="row" para destacar o nome do registro; o padrão também funciona nas tabelas de base do catálogo.

## Comportamentos específicos

- Upload seleciona arquivos, permite removê-los e emite filesChange; o envio ao servidor deve ser conectado pela página.
- PDF usa o visualizador nativo do navegador, oferece link alternativo e aceita arquivos locais ou URLs de PDFs do próprio portal. O arquivo público storybook/exemplo.pdf é apenas uma amostra.
- Gráficos usam Chart.js e oferecem tabela alternativa com os dados.
- Markdown usa marked e a sanitização padrão do Angular, sem liberar HTML inseguro.
- Editor usa Quill e emite HTML pelo formulário. A persistência futura deve validar e sanitizar o conteúdo também no back-end.
- Rolagem incremental emite loadMore e aceita loading/hasMore; inclui botão acessível como alternativa à detecção de rolagem.
- Toast usa ToastService.show(mensagem, tipo).
- Coachmark é orientação contextual por clique (não um tour completo de várias etapas).
- Links e menus do catálogo demonstram navegação; as rotas reais são fornecidas pelas páginas.

Auxiliares, Miscelânea e Restritos nas imagens são grupos fechados; seus itens internos não estão visíveis e não foram inventados. Grid e utilidades CSS possuem histórias próprias.

Novos valores de identidade visual e estilos reutilizáveis devem ficar neste diretório. Ao adicionar um componente, inclua sua história e atualize o inventário.

Confirmações destrutivas: ConfirmDialogComponent (visual-confirm-dialog) usa dialog nativo, com foco inicial em Cancelar, confirmação explícita e bloqueio das ações durante o processamento. O evento confirmed deve ser conectado à operação pela página.

## Validação dos cadastros

Os formulários usam os validators do Angular (required, minlength e maxlength), complementados por validators.ts: notBlank rejeita apenas espaços; trimmedMinlength valida o comprimento útil do login; allowedValues restringe seleções, preservando o tipo numérico do ano. O pipe validationMessage padroniza mensagens em português após o campo ficar dirty/touched ou após envio. visual-campo recebe a mensagem por erro; inputs e chips indicam aria-invalid e associam a mensagem com aria-describedby.

Matéria exige nome até 120 caracteres; turma exige ensino F/M, ano inteiro de 1 a 9 e letra A–Z; professor exige nome, login, senha e ao menos uma matéria; administrador exige login e senha. Turmas por matéria continuam opcionais. Aluno ainda não possui formulário de cadastro. O Storybook inclui um exemplo interativo de campo obrigatório.

O editor de texto permite selecionar uma figura com um clique para ajustar largura proporcional (16 a 3.000 px), texto alternativo, restaurar tamanho original, substituir ou remover. As operações usam o modelo e o histórico do Quill; os atributos width/alt permanecem no HTML ao salvar e reabrir. O exemplo do Storybook permite inserir figuras locais.

Ao selecionar a figura, quatro alças permitem redimensionar por arraste (mouse, caneta ou toque). A largura é limitada ao espaço do editor e a proporção é mantida. Setas nas alças também ajustam o tamanho; Shift usa passos de 1 px. Um único desfazer reverte cada gesto. As alças não integram o HTML salvo.

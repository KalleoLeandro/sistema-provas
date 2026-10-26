import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { CardComponent, ButtonDirective, RecordNameDirective } from '../components';
const meta = { title: 'Design system/Tabelas', decorators: [moduleMetadata({ imports: [CardComponent, ButtonDirective] })] } satisfies Meta;
export default meta;
type Story = StoryObj<typeof meta>;
export const Users: Story = { name: "Usuários", render: () => ({ template: '<visual-cartao density="compact"><div class="tabela-visual"><table><caption>Exemplo de usuários</caption><thead><tr><th scope="col">Nome</th><th scope="col">Login</th><th scope="col">Status</th><th scope="col">Ações</th></tr></thead><tbody><tr><th nomeRegistro scope="row">Administrador de exemplo</th><td>administrador.exemplo</td><td><span class="status-visual">Ativo</span></td><td><div class="acoes-registro"><button botaoVisual size="compact" variant="edit">Editar</button><button botaoVisual size="compact" variant="danger">Desativar</button></div></td></tr><tr><th nomeRegistro scope="row">Professor de exemplo</th><td>professor.exemplo</td><td><span class="status-visual inativo">Inativo</span></td><td><div class="acoes-registro"><button botaoVisual size="compact" variant="edit">Editar</button><button botaoVisual size="compact" variant="success">Ativar</button></div></td></tr></tbody></table></div></visual-cartao>' }) };

export const Subjects: Story = {
  name: 'Matérias — lista compacta',
  render: () => ({ template: '<section class="list-page-compact"><h2>Matérias</h2><visual-cartao density="compact"><div class="tabela-visual"><table><caption class="sr-only">Matérias cadastradas</caption><thead><tr><th scope="col">Nome da matéria</th></tr></thead><tbody><tr><th nomeRegistro scope="row">Matemática</th></tr><tr><th nomeRegistro scope="row">Português</th></tr></tbody></table></div></visual-cartao></section>' }),
};

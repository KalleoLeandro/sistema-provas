import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective } from '../components';
type StoryArgs = { label: string; variant: 'primary' | 'secondary' | 'edit' | 'danger' | 'success' | 'continue'; size: 'default' | 'compact'; disabled: boolean };
const meta = {
 id: "componentes-button", title: "Componentes/Botão",
 decorators: [moduleMetadata({ imports: [ButtonDirective] })],
 argTypes: { variant: { control: 'select', options: ['primary','secondary','edit','danger','success','continue'] }, size: { control: 'select', options: ['default','compact'] }, disabled: { control: 'boolean' } },
 args: { label: 'Cadastrar', variant: 'primary', size: 'default', disabled: false },
 render: (args: StoryArgs) => ({ props: args, template: '<button botaoVisual [variant]="variant" [size]="size" [disabled]="disabled">{{ label }}</button>' }),
} satisfies Meta<StoryArgs>;
export default meta;
type Story = StoryObj<StoryArgs>;
export const Primary: Story = { name: "Primário",};
export const Secondary: Story = { name: "Secundário", args: { label: 'Limpar', variant: 'secondary' } };
export const Edit: Story = { name: "Editar", args: { label: 'Editar', variant: 'edit', size: 'compact' } };
export const Deactivate: Story = { name: "Desativar", args: { label: 'Desativar', variant: 'danger', size: 'compact' } };
export const Activate: Story = { name: "Ativar", args: { label: 'Ativar', variant: 'success', size: 'compact' } };
export const Continue: Story = { name: "Cadastrar e continuar", args: { label: 'Cadastrar e continuar', variant: 'continue' } };
export const Disabled: Story = { name: "Desabilitado", args: { disabled: true } };

export const ActionsTogether: Story = {
  name: 'Comparação das ações',
  render: () => ({ template: '<div style="display:flex;flex-wrap:wrap;gap:12px;align-items:center"><button botaoVisual>Cadastrar</button><button botaoVisual variant="secondary">Visualizar</button><button botaoVisual variant="edit">Editar</button><button botaoVisual variant="continue">Cadastrar e continuar</button><button botaoVisual variant="success">Ativar</button><button botaoVisual variant="danger">Desativar</button><button botaoVisual disabled>Indisponível</button></div>' }),
};

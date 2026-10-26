import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { FieldComponent, InputDirective } from '../components';
type StoryArgs = { disabled: boolean };
const meta = {
 title: 'Design system/Campos',
 decorators: [moduleMetadata({ imports: [FieldComponent, InputDirective] })],
 args: { disabled: false },
 render: (args: StoryArgs) => ({ props: args, template: '<visual-campo rotulo="Login" identificador="exemplo-login" orientacao="De 3 a 120 caracteres."><input campoVisual id="exemplo-login" placeholder="Digite o login" [disabled]="disabled" aria-describedby="exemplo-login-orientacao" /></visual-campo>' }),
} satisfies Meta<StoryArgs>;
export default meta;
type Story = StoryObj<StoryArgs>;
export const Text: Story = { name: "Texto",};
export const Disabled: Story = { name: "Desabilitado", args: { disabled: true } };
export const Password: Story = { name: "Senha", render: () => ({ template: '<visual-campo rotulo="Senha" identificador="exemplo-senha"><input campoVisual id="exemplo-senha" type="password" placeholder="Digite a senha" /></visual-campo>' }) };
export const Dropdown: Story = { name: "Lista suspensa", render: (args: StoryArgs) => ({ props: args, template: '<visual-campo rotulo="Perfil" identificador="exemplo-perfil"><select campoVisual id="exemplo-perfil" [disabled]="disabled"><option>Administrador</option><option>Professor</option><option>Aluno</option></select></visual-campo>' }) };
export const Textarea: Story = { name: "Área de texto", render: () => ({ template: '<visual-campo rotulo="Descrição" identificador="exemplo-descricao"><textarea campoVisual id="exemplo-descricao" rows="4"></textarea></visual-campo>' }) };

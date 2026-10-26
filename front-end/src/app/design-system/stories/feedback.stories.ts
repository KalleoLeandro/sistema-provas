import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { AlertComponent, CardComponent } from '../components';
const meta = { id: "design-system-cards-e-mensagens", title: "Design system/Cartões e mensagens", decorators: [moduleMetadata({ imports: [AlertComponent, CardComponent] })] } satisfies Meta;
export default meta;
type Story = StoryObj<typeof meta>;
export const Card: Story = { name: "Cartão", render: () => ({ template: '<visual-cartao><h2>Cadastro</h2><p>Conteúdo do cartão.</p></visual-cartao>' }) };
export const CompactCard: Story = { name: "Cartão compacto", render: () => ({ template: '<visual-cartao density="compact">Conteúdo compacto para listas.</visual-cartao>' }) };
export const Error: Story = { name: "Erro", render: () => ({ template: '<visual-aviso>Não foi possível salvar. Tente novamente.</visual-aviso>' }) };
export const Success: Story = { name: "Sucesso", render: () => ({ template: '<p class="form-feedback" role="status">Cadastro realizado com sucesso.</p>' }) };

import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { InfoTooltipComponent } from '../info-tooltip';
const meta = { title: 'Componentes/Informação da tela', component: InfoTooltipComponent,
  args: { text: 'As questões são gravadas ao cadastrar. Consulte os cadastros anteriores na lista de questões.' },
} satisfies Meta<InfoTooltipComponent>;
export default meta;
export const Padrao: StoryObj<InfoTooltipComponent> = {};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTooltipModule } from '../library';
const meta = {
  id: "componentes-tooltip", title: "Componentes/Dica de contexto",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTooltipModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<button botaoVisual nz-tooltip nzTooltipTitle="Altere o login e a senha do usuário" nzTooltipTrigger="focus">Foque para ver a dica</button>',
  }),
};

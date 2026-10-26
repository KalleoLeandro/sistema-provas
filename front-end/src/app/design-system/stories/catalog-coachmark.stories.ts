import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzPopoverModule } from '../library';
const meta = {
  id: "componentes-coachmark", title: "Componentes/Orientação contextual",
  parameters: {
    docs: {
      description: {
        component: 'Orientação contextual aberta por clique, com fechamento ao clicar fora.',
      },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzPopoverModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<button botaoVisual nz-popover nzPopoverTitle="Conheça o cadastro" nzPopoverContent="Escolha um perfil antes de preencher os dados. Use Limpar para começar novamente." nzPopoverTrigger="click">Mostrar orientação</button>',
  }),
};

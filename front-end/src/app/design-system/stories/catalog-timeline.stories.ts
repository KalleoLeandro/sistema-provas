import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTimelineModule } from '../library';
const meta = {
  id: "componentes-timeline", title: "Componentes/Linha do tempo",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTimelineModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<nz-timeline><nz-timeline-item nzColor="green">Cadastro criado</nz-timeline-item><nz-timeline-item>Avaliação publicada</nz-timeline-item><nz-timeline-item nzColor="gray">Resultado disponível</nz-timeline-item></nz-timeline>',
  }),
};

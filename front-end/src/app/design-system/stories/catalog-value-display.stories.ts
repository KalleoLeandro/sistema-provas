import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzStatisticModule } from '../library';
const meta = {
  id: "componentes-value-display", title: "Componentes/Exibição de valores",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzStatisticModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<div class="ds-row"><nz-statistic nzTitle="Alunos" [nzValue]="128" /><nz-statistic nzTitle="Aproveitamento" [nzValue]="85" nzSuffix="%" /></div>',
  }),
};

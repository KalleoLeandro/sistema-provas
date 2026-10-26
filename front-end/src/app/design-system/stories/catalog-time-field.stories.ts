import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTimePickerModule } from '../library';
const meta = {
  id: "componentes-time-field", title: "Componentes/Campo de horário",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTimePickerModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ value: null } },
    template:
      '<label class="ds-stack">Horário da avaliação<nz-time-picker [(ngModel)]="value" nzFormat="HH:mm" nzPlaceHolder="Selecione o horário" /></label>',
  }),
};

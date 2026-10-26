import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { DateFieldComponent } from '../date-field';
const meta = {
  id: "componentes-date-field", title: "Componentes/Campo de data",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, DateFieldComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ value: null } },
    template:
      '<label class="ds-stack">Data da avaliação<visual-date-field [(ngModel)]="value" inputId="exemplo-data" /><span>Data selecionada: {{value ? value : "Nenhuma"}}</span></label>',
  }),
};

export const DataEHora:StoryObj={name:'Data e hora',render:()=>({props:{value:'2026-10-25T15:30:00.000Z'},template:'<visual-date-field [(ngModel)]="value" [showTime]="true" inputId="exemplo-data-hora" placeholder="Selecione data e horário" />'})};

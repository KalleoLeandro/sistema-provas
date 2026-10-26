import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzInputNumberModule } from '../library';
const meta = {
  id: "componentes-money-field", title: "Componentes/Campo monetário",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzInputNumberModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{ amount: 1250.5 },
      moneyFormatter: (value: number) =>
        value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      moneyParser: (value: string) => Number(value.replace(/[^0-9,-]/g, '').replace(',', '.')),
    },
    template:
      '<label class="ds-stack">Valor em reais<nz-input-number [(ngModel)]="amount" [nzMin]="0" [nzPrecision]="2" [nzStep]="0.01" nzPrefix="R$" [nzFormatter]="moneyFormatter" [nzParser]="moneyParser" /><span>Valor numérico: {{amount}}</span></label>',
  }),
};

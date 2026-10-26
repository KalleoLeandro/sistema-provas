import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzInputNumberModule } from '../library';
const meta = {
  id: "componentes-number-field", title: "Componentes/Campo numérico",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
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
    props: { ...{ quantity: 10 } },
    template:
      '<label class="ds-stack">Quantidade de questões<nz-input-number [(ngModel)]="quantity" [nzMin]="1" [nzMax]="100" [nzStep]="1" /></label>',
  }),
};

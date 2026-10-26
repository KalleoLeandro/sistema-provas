import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSliderModule } from '../library';
const meta = {
  id: "componentes-slider", title: "Componentes/Controle deslizante",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSliderModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ value: 6 } },
    template:
      '<label class="ds-stack">Nota mínima<nz-slider [(ngModel)]="value" [nzMin]="0" [nzMax]="10" [nzStep]="0.5" /><span>{{value}}</span></label>',
  }),
};

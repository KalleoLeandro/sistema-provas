import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzRateModule } from '../library';
const meta = {
  id: "componentes-rating", title: "Componentes/Avaliação por estrelas",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzRateModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ rating: 4 } },
    template:
      '<label class="ds-stack">Avaliação do conteúdo<nz-rate [(ngModel)]="rating" /><span>{{rating}} de 5</span></label>',
  }),
};

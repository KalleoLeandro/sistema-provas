import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTypographyModule } from '../library';
const meta = {
  id: "componentes-typography", title: "Componentes/Tipografia",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTypographyModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<h1 nz-typography>ESFÍNORA</h1><h2 nz-typography>Avaliação e conhecimento</h2><p nz-typography>Texto principal para leitura.</p><p nz-typography nzType="secondary">Texto complementar.</p><p nz-typography><strong>Destaque</strong> e <em>ênfase</em>.</p>',
  }),
};

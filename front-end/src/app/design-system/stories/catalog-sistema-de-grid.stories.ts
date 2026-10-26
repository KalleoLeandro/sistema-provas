import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  id: "componentes-sistema-de-grid", title: "Componentes/Sistema de grade",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({ imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<div class="ds-grid"><div class="ds-col-4 ds-panel">4 colunas</div><div class="ds-col-4 ds-panel">4 colunas</div><div class="ds-col-4 ds-panel">4 colunas</div><div class="ds-col-6 ds-panel">6 colunas</div><div class="ds-col-6 ds-panel">6 colunas</div><div class="ds-col-12 ds-panel">12 colunas</div></div>',
  }),
};

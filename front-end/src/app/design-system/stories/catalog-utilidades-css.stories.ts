import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  title: "Componentes/Utilidades CSS",
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
      '<div class="ds-stack"><div class="ds-row"><span class="ds-panel">Linha flexível</span><span class="ds-panel">Com espaçamento</span></div><p class="ds-muted">Texto secundário</p><p class="ds-ellipsis" style="max-width:240px">Texto longo com reticências quando não houver espaço disponível.</p></div>',
  }),
};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  id: "componentes-text-field", title: "Componentes/Campo de texto",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
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
      '<visual-campo rotulo="Nome" identificador="text-field"><input campoVisual id="text-field" placeholder="Digite o nome" maxlength="120" /></visual-campo>',
  }),
};

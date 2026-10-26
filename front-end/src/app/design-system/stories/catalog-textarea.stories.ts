import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  id: "componentes-textarea", title: "Componentes/Área de texto",
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
      '<visual-campo rotulo="Descrição" identificador="textarea-field"><textarea campoVisual id="textarea-field" rows="5" maxlength="1000" placeholder="Descreva o conteúdo"></textarea></visual-campo>',
  }),
};

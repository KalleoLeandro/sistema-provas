import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { FieldsetComponent } from '../library';
const meta = {
  id: "componentes-fieldset", title: "Componentes/Conjunto de campos",
  decorators: [
    moduleMetadata({ imports: [FormsModule, InputDirective, ButtonDirective, FieldsetComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-fieldset label="Dados de acesso"><label class="ds-stack">Login<input campoVisual /></label></visual-fieldset>',
  }),
};

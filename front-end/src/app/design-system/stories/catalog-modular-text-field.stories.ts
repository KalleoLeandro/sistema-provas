import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { ModularFieldComponent } from '../library';
const meta = {
  id: "componentes-modular-text-field", title: "Componentes/Campo com prefixo e sufixo",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, ModularFieldComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-modular-field prefix="Código" suffix="/2026"><input campoVisual aria-label="Número do registro" placeholder="0001" /></visual-modular-field>',
  }),
};

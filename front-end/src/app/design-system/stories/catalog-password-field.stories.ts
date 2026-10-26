import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { PasswordFieldComponent } from '../library';
const meta = {
  id: "componentes-password-field", title: "Componentes/Campo de senha",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, PasswordFieldComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({ props: { message: '' }, template: '<visual-password-field ngModel />' }),
};

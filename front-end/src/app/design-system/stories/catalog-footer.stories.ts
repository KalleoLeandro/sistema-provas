import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { FooterComponent } from '../library';
const meta = {
  id: "componentes-footer", title: "Componentes/Rodapé",
  decorators: [
    moduleMetadata({ imports: [FormsModule, InputDirective, ButtonDirective, FooterComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({ props: { message: '' }, template: '<visual-footer />' }),
};

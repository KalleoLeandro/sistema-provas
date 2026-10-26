import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { IconActionComponent } from '../library';
const meta = {
  id: "componentes-icon-action", title: "Componentes/Botão com ícone",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, IconActionComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-icon-action label="Editar cadastro" icon="edit" (action)="message = \'Edição solicitada\'" /><p role="status">{{message}}</p>',
  }),
};

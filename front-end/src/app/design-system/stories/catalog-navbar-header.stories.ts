import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { NavbarComponent } from '../library';
const meta = {
  id: "componentes-navbar-header", title: "Componentes/Barra de navegação",
  decorators: [
    moduleMetadata({ imports: [FormsModule, InputDirective, ButtonDirective, NavbarComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-navbar><span>Bem-vindo, Administrador</span><button botaoVisual variant="secondary" size="compact" (click)="message = \'Saída solicitada\'">Sair</button></visual-navbar><p role="status">{{message}}</p>',
  }),
};

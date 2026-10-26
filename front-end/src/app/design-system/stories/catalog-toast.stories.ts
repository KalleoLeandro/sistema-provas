import { Component, inject } from '@angular/core';
import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective } from '../components';
import { ToastService } from '../toast.service';
@Component({
  selector: 'ds-toast-example',
  imports: [ButtonDirective],
  template: '<button botaoVisual (click)="show()">Exibir notificação</button>',
})
class ToastExampleComponent {
  private readonly toast = inject(ToastService);
  show() {
    this.toast.show('Cadastro realizado com sucesso.', 'success');
  }
}
const meta = {
  id: "componentes-toast", title: "Componentes/Notificação",
  component: ToastExampleComponent,
} satisfies Meta<ToastExampleComponent>;
export default meta;
export const Default: StoryObj<ToastExampleComponent> = { name: 'Padrão' };

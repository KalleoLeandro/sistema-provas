import { Component, input, output } from '@angular/core';
import { ButtonDirective } from './components';

@Component({
  selector: 'visual-form-actions',
  imports: [ButtonDirective],
  template: `<div class="form-actions">
    <button botaoVisual variant="secondary" type="button" [disabled]="busy()" (click)="clear.emit()">Limpar</button>
    <button botaoVisual type="submit" [disabled]="busy() || invalid()">{{ busy() ? 'Cadastrando…' : 'Cadastrar' }}</button>
    <button botaoVisual variant="continue" type="button" [disabled]="busy() || invalid()" (click)="saveAndContinue.emit()">Cadastrar e continuar</button>
  </div>`,
  styles: [':host{display:block}'],
})
export class FormActionsComponent {
  readonly busy = input(false);
  readonly invalid = input(false);
  readonly clear = output<void>();
  readonly saveAndContinue = output<void>();
}

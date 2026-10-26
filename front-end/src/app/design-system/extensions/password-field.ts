import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { FieldComponent, InputDirective, ButtonDirective } from '../components';
let nextId = 0;
@Component({
  selector: 'visual-password-field',
  imports: [FieldComponent, InputDirective, ButtonDirective],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordFieldComponent),
      multi: true,
    },
  ],
  template: `<visual-campo [rotulo]="label" [identificador]="id"
    ><div class="ds-row">
      <input
        campoVisual
        [id]="id"
        [type]="visible ? 'text' : 'password'"
        [value]="value"
        [disabled]="disabled"
        (input)="change($event)"
        (blur)="touched()"
      /><button
        botaoVisual
        variant="secondary"
        type="button"
        [disabled]="disabled"
        [attr.aria-controls]="id"
        [attr.aria-pressed]="visible"
        (click)="visible = !visible"
      >
        {{ visible ? 'Ocultar' : 'Mostrar' }}
      </button>
    </div></visual-campo
  >`,
})
export class PasswordFieldComponent implements ControlValueAccessor {
  @Input() label = 'Senha';
  @Input() id = 'visual-password-' + nextId++;
  value = '';
  disabled = false;
  visible = false;
  private changed: (value: string) => void = () => {};
  touched: () => void = () => {};
  writeValue(value: string | null) {
    this.value = value ?? '';
  }
  registerOnChange(fn: (value: string) => void) {
    this.changed = fn;
  }
  registerOnTouched(fn: () => void) {
    this.touched = fn;
  }
  setDisabledState(value: boolean) {
    this.disabled = value;
    if (value) this.visible = false;
  }
  change(event: Event) {
    this.value = (event.target as HTMLInputElement).value;
    this.changed(this.value);
  }
}

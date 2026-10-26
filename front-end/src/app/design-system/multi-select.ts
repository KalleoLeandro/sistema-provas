import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export interface MultiSelectOption { value: number; label: string; }

@Component({
  selector: 'visual-multi-select',
  templateUrl: './multi-select.html',
  styleUrl: './multi-select.css',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => MultiSelectComponent), multi: true }],
})
export class MultiSelectComponent implements ControlValueAccessor {
  @Input() options: MultiSelectOption[] = [];
  @Input() label = 'Opções';
  @Input() emptyMessage = 'Nenhuma opção disponível.';
  value: number[] = [];
  disabled = false;
  private onChange: (value: number[]) => void = () => {};
  onTouched: () => void = () => {};

  /** Atualiza a seleção apresentada quando o formulário recebe um novo valor. */
  writeValue(value: number[] | null): void {
    /* Uma cópia impede alterações acidentais no array pertencente ao formulário. */
    this.value = [...(value ?? [])];
  }

  registerOnChange(fn: (value: number[]) => void): void { this.onChange = fn; }
  registerOnTouched(fn: () => void): void { this.onTouched = fn; }
  setDisabledState(disabled: boolean): void { this.disabled = disabled; }

  /** Alterna uma opção e informa o formulário sem permitir seleções repetidas. */
  toggle(value: number): void {
    /* Opções indisponíveis e alterações durante o envio não podem modificar o cadastro. */
    if (this.disabled || !this.options.some(option => option.value === value)) return;
    this.value = this.value.includes(value) ? this.value.filter(item => item !== value) : [...this.value, value];
    this.onChange([...this.value]);
    this.onTouched();
  }
}

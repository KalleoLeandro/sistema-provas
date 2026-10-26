import { Component, forwardRef, Input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { InputDirective, ButtonDirective } from '../components';
@Component({
  selector: 'visual-tag-field',
  imports: [InputDirective, ButtonDirective],
  providers: [
    { provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => TagFieldComponent), multi: true },
  ],
  template: `<div class="ds-stack">
    <div class="ds-row" aria-live="polite">
      @for (tag of value(); track tag) {
        <span class="ds-panel"
          >{{ tag }}
          <button
            botaoVisual
            size="compact"
            variant="secondary"
            type="button"
            [disabled]="disabled"
            [attr.aria-label]="'Remover ' + tag"
            (click)="remove(tag)"
          >
            ?
          </button></span
        >
      }
    </div>
    <input
      campoVisual
      [attr.aria-label]="label"
      [disabled]="disabled"
      placeholder="Digite e pressione Enter"
      (keydown.enter)="add($event)"
      (blur)="onTouched()"
    />
  </div>`,
})
export class TagFieldComponent implements ControlValueAccessor {
  @Input() label = 'Adicionar marcador';
  readonly value = signal<string[]>([]);
  disabled = false;
  onChange: (value: string[]) => void = () => {};
  onTouched: () => void = () => {};
  writeValue(value: string[] | null) {
    this.value.set([...new Set(value ?? [])]);
  }
  registerOnChange(fn: (value: string[]) => void) {
    this.onChange = fn;
  }
  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }
  setDisabledState(value: boolean) {
    this.disabled = value;
  }
  add(event: Event) {
    event.preventDefault();
    if (this.disabled) return;
    const input = event.target as HTMLInputElement;
    const tag = input.value.trim();
    if (tag && !this.value().includes(tag)) {
      this.value.update((tags) => [...tags, tag]);
      this.onChange(this.value());
    }
    input.value = '';
    this.onTouched();
  }
  remove(tag: string) {
    if (this.disabled) return;
    this.value.update((tags) => tags.filter((value) => value !== tag));
    this.onChange(this.value());
    this.onTouched();
  }
}

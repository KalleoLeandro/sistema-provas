import { Directive, ElementRef, forwardRef, HostListener, inject } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

/** Peso numérico com separador brasileiro e uma casa decimal. */
@Directive({selector: 'input[weightMask]', providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => WeightMaskDirective), multi: true}]})
export class WeightMaskDirective implements ControlValueAccessor {
  private readonly element = inject<ElementRef<HTMLInputElement>>(ElementRef);
  private change: (value: number | null) => void = () => {};
  private touched = () => {};
  writeValue(value: number | null): void { this.element.nativeElement.value = value == null ? '' : Number(value).toFixed(1).replace('.', ','); }
  registerOnChange(fn: (value: number | null) => void): void { this.change = fn; }
  registerOnTouched(fn: () => void): void { this.touched = fn; }
  setDisabledState(disabled: boolean): void { this.element.nativeElement.disabled = disabled; }
  @HostListener('input') input(): void {
    const field = this.element.nativeElement;
    const clean = field.value.replace(/\./g, ',').replace(/[^0-9,]/g, '');
    const [whole, ...fraction] = clean.split(',');
    field.value = whole.slice(0, 6) + (fraction.length ? ',' + fraction.join('').slice(0, 1) : '');
    this.change(field.value === '' || field.value === ',' ? null : Number(field.value.replace(',', '.')));
  }
  @HostListener('blur') blur(): void {
    const value = this.element.nativeElement.value;
    if (value && value !== ',') this.writeValue(Number(value.replace(',', '.')));
    this.touched();
  }
}

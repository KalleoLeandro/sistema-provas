import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ChipSelectComponent } from './chip-select';

@Component({ imports: [FormsModule, ChipSelectComponent], template: '<visual-chip-select inputId="materias-teste" label="Matérias" [options]="options" [(ngModel)]="value" [disabled]="disabled()" />' })
class HostComponent {
  options = [{ value: 1, label: 'Matemática' }, { value: 2, label: 'História' }];
  value = signal<number[]>([]);
  disabled = signal(false);
}

describe('Seleção com chips', () => {
  it('busca sem acentos, seleciona com Enter e permite remover o chip', async () => {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'matematica'; input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('[role=option]')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('[role=option]').textContent).toContain('Matemática');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.value()).toEqual([1]);
    expect(fixture.nativeElement.querySelector('.chip').textContent).toContain('Matemática');
    expect(input.value).toBe('');
    (fixture.nativeElement.querySelector('.chip button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.componentInstance.value()).toEqual([]);
  });

  it('não transforma texto livre em seleção e bloqueia alterações quando desabilitado', async () => {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'não cadastrada'; input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.value()).toEqual([]);
    fixture.componentInstance.value.set([1]); fixture.componentInstance.disabled.set(true);
    await fixture.whenStable();
    expect(input.disabled).toBe(true);
    expect((fixture.nativeElement.querySelector('.chip button') as HTMLButtonElement).disabled).toBe(true);
  });

  it('descarta busca e chips ao limpar e navega nas sugestões com setas e Escape', async () => {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', cancelable: true }));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', cancelable: true }));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.value()).toEqual([2]);
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', cancelable: true }));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role=listbox]')).toBeNull();
    fixture.componentInstance.value.set([]);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.chip')).toBeNull();
    expect(input.value).toBe('');
  });
});

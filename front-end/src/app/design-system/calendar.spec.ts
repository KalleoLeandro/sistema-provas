import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { NzCalendarModule } from 'ng-zorro-antd/calendar';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzTimePickerModule } from 'ng-zorro-antd/time-picker';
import { NzDateAdapter } from 'ng-zorro-antd/core/time';
import { provideDesignSystem } from './providers';

@Component({ imports: [FormsModule, NzCalendarModule, NzDatePickerModule, NzTimePickerModule], template: `
  <nz-calendar [(ngModel)]="selected" [nzFullscreen]="false" />
  <nz-date-picker [(ngModel)]="selected" nzFormat="dd/MM/yyyy" />
  <nz-time-picker [(ngModel)]="selected" nzFormat="HH:mm" />
` })
class DateControlsExample { selected = new Date(2026, 9, 23, 14, 30); }

describe('Datas do design system', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideHttpClient(), provideDesignSystem()] }));

  it('renderiza calendário, data e horário com o provider compartilhado', async () => {
    const fixture = TestBed.createComponent(DateControlsExample);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('nz-calendar td').length).toBeGreaterThanOrEqual(28);
    expect(element.querySelector('nz-date-picker input')?.getAttribute('placeholder')).toBeTruthy();
    expect((element.querySelector('nz-date-picker input') as HTMLInputElement).value).toBe('23/10/2026');
    expect((element.querySelector('nz-time-picker input') as HTMLInputElement).value).toBe('14:30');
  });

  it('compartilha o adaptador com formato e nomes de meses em português', () => {
    const adapter = TestBed.inject(NzDateAdapter);
    expect(adapter.format(new Date(2026, 9, 23), 'dd/MM/yyyy')).toBe('23/10/2026');
    expect(adapter.getMonthNames('long')[9]).toBe('outubro');
  });
});

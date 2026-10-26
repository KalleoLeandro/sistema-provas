import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { By } from '@angular/platform-browser';
import { DateFieldComponent } from './date-field';
import { provideDesignSystem } from './providers';
@Component({imports:[FormsModule,DateFieldComponent],template:'<form><visual-date-field name="data" [(ngModel)]="value" [showTime]="true" inputId="data-exemplo" required /></form>'})
class Example{value='2026-10-25T15:30:00.000Z';}
describe('Campo de data e hora',()=>{
 beforeEach(()=>TestBed.configureTestingModule({providers:[provideHttpClient(),provideDesignSystem()]}));
 it('recebe ISO sem modificar o instante original e renderiza seletor do design system',async()=>{const f=TestBed.createComponent(Example);await f.whenStable();const c=f.debugElement.query(By.directive(DateFieldComponent)).componentInstance as DateFieldComponent;expect(c.value?.toISOString()).toBe(f.componentInstance.value);expect(f.nativeElement.querySelector('#data-exemplo').type).toBe('text');expect(f.nativeElement.querySelector('nz-date-picker')).toBeTruthy();});
 it('emite ISO ao selecionar e vazio ao limpar',async()=>{const f=TestBed.createComponent(Example);await f.whenStable();const c=f.debugElement.query(By.directive(DateFieldComponent)).componentInstance as DateFieldComponent;c.changed(new Date('2026-10-26T17:45:00Z'));await f.whenStable();expect(f.componentInstance.value).toBe('2026-10-26T17:45:00.000Z');c.changed(null);await f.whenStable();expect(f.componentInstance.value).toBe('');});
});

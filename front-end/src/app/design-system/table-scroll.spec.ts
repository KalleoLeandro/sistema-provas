import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { TableScrollComponent } from './table-scroll';
@Component({imports:[TableScrollComponent],template:'<visual-table-scroll><div class="tabela-visual"><table><tbody><tr><td>Exemplo</td></tr></tbody></table></div></visual-table-scroll>'})
class TestHost {}
describe('Rolagem horizontal persistente',()=>{
  it('sincroniza o controle com a tabela e mantém navegação pelo teclado',async()=>{
    const f=TestBed.createComponent(TestHost);await f.whenStable();
    const viewport=f.nativeElement.querySelector('.tabela-visual') as HTMLElement;
    Object.defineProperty(viewport,'clientWidth',{configurable:true,value:300});
    Object.defineProperty(viewport,'scrollWidth',{configurable:true,value:1000});
    const c=f.debugElement.query(By.directive(TableScrollComponent)).componentInstance as TableScrollComponent;
    c.update();await f.whenStable();
    const input=f.nativeElement.querySelector('input') as HTMLInputElement;
    expect(input).toBeTruthy();expect(input.max).toBe('700');
    input.value='250';input.dispatchEvent(new Event('input'));await f.whenStable();expect(viewport.scrollLeft).toBe(250);
    viewport.scrollLeft=400;viewport.dispatchEvent(new Event('scroll'));await f.whenStable();expect(input.value).toBe('400');
    input.dispatchEvent(new KeyboardEvent('keydown',{key:'End',bubbles:true}));await f.whenStable();expect(viewport.scrollLeft).toBe(700);
    Object.defineProperty(viewport,'clientWidth',{configurable:true,value:1000});c.update();await f.whenStable();
    expect(f.nativeElement.querySelector('.table-scroll-control')).toBeNull();
  });
});

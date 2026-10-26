import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { NavigationMenuComponent } from './navigation-menu';

describe('Submenus laterais em cascata',()=>{
  beforeEach(()=>TestBed.configureTestingModule({providers:[provideRouter([])]}));
  async function setup(){
    const f=TestBed.createComponent(NavigationMenuComponent);
    f.componentRef.setInput('items',[{id:'cadastros',label:'Cadastros',children:[{id:'usuarios',label:'Usuários',children:[{id:'listar',label:'Listar',route:'/cadastro/usuarios/listar'}]}]}]);
    await f.whenStable();return f;
  }
  it('abre dois níveis e fecha todos ao clicar fora',async()=>{
    const f=await setup(); const root=f.nativeElement.querySelector('button') as HTMLButtonElement;
    root.click();await f.whenStable(); expect(root.getAttribute('aria-expanded')).toBe('true');
    expect(document.querySelectorAll('.navigation-panel')).toHaveLength(1);
    root.click();await f.whenStable();expect(document.querySelectorAll('.navigation-panel')).toHaveLength(0);
    root.click();await f.whenStable();
    (document.querySelector('.navigation-panel button') as HTMLButtonElement).click();await f.whenStable();
    expect(document.querySelectorAll('.navigation-panel')).toHaveLength(2);
    document.body.click();await f.whenStable();
    expect(document.querySelectorAll('.navigation-panel')).toHaveLength(0);expect(root.getAttribute('aria-expanded')).toBe('false');
  });
  it('emite a navegação uma única vez e recolhe a cascata ao escolher um link',async()=>{
    vi.spyOn(TestBed.inject(Router),'navigateByUrl').mockResolvedValue(true);
    const f=await setup();const navigate=vi.fn();f.componentInstance.navigate.subscribe(navigate);
    (f.nativeElement.querySelector('button') as HTMLButtonElement).click();await f.whenStable();
    (document.querySelector('.navigation-panel button') as HTMLButtonElement).click();await f.whenStable();
    (document.querySelector('.navigation-panel a') as HTMLAnchorElement).click();await f.whenStable();
    expect(navigate).toHaveBeenCalledTimes(1);expect(document.querySelectorAll('.navigation-panel')).toHaveLength(0);
  });
});

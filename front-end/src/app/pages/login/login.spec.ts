import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { LoginComponent } from './login';
import { AuthService } from '../../services/auth.service';

describe('Redirecionamento após login', () => {
  const login = vi.fn();
  const navigateByUrl = vi.fn();
  beforeEach(async () => {
    login.mockReset(); navigateByUrl.mockReset();
    navigateByUrl.mockResolvedValue(true);
    await TestBed.configureTestingModule({ imports: [LoginComponent], providers: [
      { provide: AuthService, useValue: { login } },
      { provide: Router, useValue: { navigateByUrl } },
    ] }).compileComponents();
  });

  async function submit(profileIndex = 2) {
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    (element.querySelectorAll('.perfil')[profileIndex] as HTMLButtonElement).click();
    await fixture.whenStable();
    for (const [id, value] of [['entrada-usuario', 'administrador'], ['entrada-senha', 'senha-teste']]) {
      const input = element.querySelector('#' + id) as HTMLInputElement;
      input.value = value; input.dispatchEvent(new Event('input'));
    }
    await fixture.whenStable();
    element.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    return fixture;
  }

  it('abre dashboard apenas após autenticação e limpa credenciais', async () => {
    login.mockResolvedValue(undefined);
    const fixture = await submit();
    expect(login).toHaveBeenCalledWith('administrador', 'senha-teste', 'administrador');
    expect(navigateByUrl).toHaveBeenCalledWith('/dashboard');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.componentInstance.usuario).toBe('');
  });
  it('permanece no login quando a autenticação falha', async () => {
    login.mockRejectedValue(new Error('Falha'));
    const fixture = await submit();
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.componentInstance.mensagem).toContain('Não foi possível entrar');
  });
  it('autentica professor no perfil selecionado e abre dashboard', async () => {
    login.mockResolvedValue(undefined);
    const fixture = await submit(1);
    expect(login).toHaveBeenCalledWith('administrador', 'senha-teste', 'professor');
    expect(navigateByUrl).toHaveBeenCalledWith('/dashboard');
    expect(fixture.componentInstance.senha).toBe('');
  });
  it('envia campos preenchidos por autofill sem eventos de input', async () => {
    login.mockResolvedValue(undefined);
    const fixture = TestBed.createComponent(LoginComponent); await fixture.whenStable();
    (fixture.nativeElement.querySelectorAll('.perfil')[1] as HTMLButtonElement).click(); await fixture.whenStable();
    (fixture.nativeElement.querySelector('#entrada-usuario') as HTMLInputElement).value = 'portugues.a';
    (fixture.nativeElement.querySelector('#entrada-senha') as HTMLInputElement).value = 'Teste@12345';
    expect(fixture.componentInstance.usuario).toBe('');
    (fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(login).toHaveBeenCalledWith('portugues.a', 'Teste@12345', 'professor');
    expect(navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });
  it('explica o bloqueio sem chamar a API quando as credenciais estão incompletas', async () => {
    const fixture = TestBed.createComponent(LoginComponent); await fixture.whenStable();
    (fixture.nativeElement.querySelectorAll('.perfil')[1] as HTMLButtonElement).click(); await fixture.whenStable();
    (fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#entrada-usuario-erro').textContent).toContain('obrigatório');
    expect(fixture.nativeElement.querySelector('#entrada-senha-erro').textContent).toContain('obrigatório');
    expect(login).not.toHaveBeenCalled();
    expect(navigateByUrl).not.toHaveBeenCalled();
  });
  it('autentica aluno somente com CGM e abre suas provas',async()=>{
    login.mockResolvedValue(undefined);const f=TestBed.createComponent(LoginComponent);await f.whenStable();
    const input=f.nativeElement.querySelector('#entrada-cgm') as HTMLInputElement;input.value='00123';input.dispatchEvent(new Event('input'));
    await f.whenStable();f.nativeElement.querySelector('form').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));await f.whenStable();
    expect(login).toHaveBeenCalledWith('00123','','aluno');expect(navigateByUrl).toHaveBeenCalledWith('/aluno/provas');expect(f.componentInstance.cgm).toBe('');
  });

  it('não marca erro ao navegar com Tab ou digitar antes de submeter',async()=>{
    const f=TestBed.createComponent(LoginComponent);await f.whenStable();
    (f.nativeElement.querySelectorAll('.perfil')[1] as HTMLButtonElement).click();await f.whenStable();
    const usuario=f.nativeElement.querySelector('#entrada-usuario') as HTMLInputElement;
    const senha=f.nativeElement.querySelector('#entrada-senha') as HTMLInputElement;
    usuario.value='professor';usuario.dispatchEvent(new Event('input'));usuario.dispatchEvent(new Event('blur'));senha.focus();
    senha.value='123';senha.dispatchEvent(new Event('input'));senha.dispatchEvent(new Event('blur'));await f.whenStable();
    expect(senha.getAttribute('aria-invalid')).toBeNull();expect(f.nativeElement.querySelector('#entrada-senha-erro')).toBeNull();expect(f.nativeElement.querySelector('visual-aviso')).toBeNull();expect(login).not.toHaveBeenCalled();
    (f.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).click();await f.whenStable();
    expect(senha.getAttribute('aria-invalid')).toBe('true');expect(f.nativeElement.querySelector('#entrada-senha-erro').textContent).not.toBe('');expect(login).not.toHaveBeenCalled();
  });
});

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AuthService } from './services/auth.service';
import { LoginComponent } from './pages/login/login';

describe('Tela de login', () => {
    beforeEach(async () => { await TestBed.configureTestingModule({ imports: [LoginComponent], providers: [provideHttpClient(), provideRouter([]),{provide:AuthService,useValue:{login:()=>Promise.reject(new Error("Falha"))}}] }).compileComponents(); });
    it('exibe apenas CGM para aluno e limpa dados ao trocar de perfil', async () => {
        const tela = TestBed.createComponent(LoginComponent);
        await tela.whenStable();
        const elemento = tela.nativeElement as HTMLElement;
        expect(elemento.querySelector('#entrada-cgm')).toBeTruthy();
        expect(elemento.querySelector('input[type="password"]')).toBeNull();
        const campo = elemento.querySelector('input')!;
        campo.value = '123456'; campo.dispatchEvent(new Event('input'));
        await tela.whenStable();
        (elemento.querySelectorAll('.perfil')[1] as HTMLButtonElement).click();
        await tela.whenStable();
        expect(tela.componentInstance.cgm).toBe('');
        expect(elemento.querySelector('#entrada-cgm')).toBeNull();
        expect(elemento.querySelector('#entrada-usuario')).toBeTruthy();
        expect(elemento.querySelector('input[type="password"]')).toBeTruthy();
    });
    it('valida o CGM e limpa o formulário após uma falha de autenticação', async () => {
        const tela = TestBed.createComponent(LoginComponent);
        await tela.whenStable();
        const elemento = tela.nativeElement as HTMLElement;
        const campo = elemento.querySelector('input')!;
        campo.value = 'abc'; campo.dispatchEvent(new Event('input'));
        await tela.whenStable();
        (elemento.querySelector('[type="submit"]') as HTMLButtonElement).click();
        await tela.whenStable();
        expect(elemento.querySelector('#entrada-cgm-erro')?.textContent).toContain('inválido');
        campo.value = '123456'; campo.dispatchEvent(new Event('input'));
        await tela.whenStable();
        expect((elemento.querySelector('[type="submit"]') as HTMLButtonElement).disabled).toBe(false);
        elemento.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
        await tela.whenStable();
        expect(campo.value).toBe('');
        expect(elemento.querySelector('visual-aviso[role="alert"]')?.textContent).toContain('Não foi possível entrar');
    });
});

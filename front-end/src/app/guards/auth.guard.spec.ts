import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';

describe('Acesso ao dashboard', () => {
  const restore = vi.fn();
  beforeEach(() => {
    restore.mockReset();
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: AuthService, useValue: { restore } }] });
  });
  const check = () => TestBed.runInInjectionContext(() => authGuard(
    { data: { perfis: ['administrador'] } } as unknown as ActivatedRouteSnapshot,
    { url: '/cadastro/materias/listar' } as RouterStateSnapshot));

  it('preserva o destino e não envia ao login durante falha de conexão', async () => {
    restore.mockRejectedValue(new Error('Servidor indisponível'));
    const result = await check();
    const url = TestBed.inject(Router).serializeUrl(result as ReturnType<Router['createUrlTree']>);
    expect(url).toContain('/indisponivel?retorno='); expect(url).not.toContain('/login');
  });
  it('envia visitante sem sessão ao login', async () => {
    restore.mockResolvedValue(null);
    const result = await check();
    expect(TestBed.inject(Router).serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/login');
  });
  it('permite acesso administrativo após restaurar sessão', async () => {
    restore.mockResolvedValue({ perfil: 'administrador' });
    expect(await check()).toBe(true);
  });
  it('direciona professor para sua página inicial', async () => {
    restore.mockResolvedValue({ perfil: 'professor' });
    const result = await check();
    expect(TestBed.inject(Router).serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/dashboard');
  });
});

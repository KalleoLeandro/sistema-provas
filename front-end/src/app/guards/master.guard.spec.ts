import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot } from '@angular/router';
import { vi } from 'vitest';
import { DesignSystemService } from '../services/design-system.service';
import { masterGuard } from './master.guard';

describe('Acesso exclusivo do master', () => {
  const checkAccess = vi.fn();
  beforeEach(() => {
    checkAccess.mockReset();
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: DesignSystemService, useValue: { checkAccess } }] });
  });
  const check = () => TestBed.runInInjectionContext(() => masterGuard({} as ActivatedRouteSnapshot,
    { url: '/design-system' } as RouterStateSnapshot));

  it('abre a página com permissão confirmada', async () => {
    checkAccess.mockResolvedValue(true);
    expect(await check()).toBe(true);
  });

  it('retorna ao dashboard quando não há permissão', async () => {
    checkAccess.mockResolvedValue(false);
    expect(TestBed.inject(Router).serializeUrl(await check() as ReturnType<Router['createUrlTree']>)).toBe('/dashboard');
  });

  it('oferece nova tentativa em falhas do servidor sem enviar ao login', async () => {
    checkAccess.mockRejectedValue(new Error('Indisponível'));
    const url = TestBed.inject(Router).serializeUrl(await check() as ReturnType<Router['createUrlTree']>);
    expect(url).toContain('/indisponivel?retorno=');
    expect(url).not.toContain('/login');
  });
});

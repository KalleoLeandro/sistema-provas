import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { vi } from 'vitest';
import { rotas } from './app.routes';
import { AuthService } from './services/auth.service';
import { NaoEncontradaComponent } from './pages/nao-encontrada/nao-encontrada';

describe('Rotas inexistentes', () => {
  const restore = vi.fn();
  const usuario = vi.fn();

  beforeEach(() => {
    restore.mockReset();
    usuario.mockReset();
    TestBed.configureTestingModule({
      providers: [provideRouter(rotas), { provide: AuthService, useValue: { restore, usuario } }],
    });
  });

  it.each(['/pagina-inexistente', '/cadastro/turmas/rota-inexistente'])(
    'exibe o erro 404 em %s sem consultar ou alterar a sessão',
    async (url) => {
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl(url, NaoEncontradaComponent);
      expect(TestBed.inject(Router).url).toBe(url);
      expect(harness.routeNativeElement?.textContent).toContain('Página não encontrada');
      expect(harness.routeNativeElement?.querySelector('a')?.getAttribute('href')).toBe('/');
      expect(restore).not.toHaveBeenCalled();
      expect(usuario).not.toHaveBeenCalled();
    },
  );
});

import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { Usuario } from '../models/usuario';

describe('Preservação da autenticação', () => {
  const get = vi.fn();
  const usuario: Usuario = {
    id: 1,
    nome: 'Administrador',
    login: 'administrador',
    perfil: 'administrador',
  };
  beforeEach(() => {
    get.mockReset();
    TestBed.configureTestingModule({ providers: [{ provide: HttpClient, useValue: { get } }] });
  });
  for (const status of [0, 404, 502, 503])
    it('não limpa a sessão em erro ' + status, async () => {
      get.mockReturnValue(throwError(() => new HttpErrorResponse({ status })));
      const auth = TestBed.inject(AuthService);
      auth.usuario.set(usuario);
      await expect(auth.restore()).rejects.toBeInstanceOf(HttpErrorResponse);
      expect(auth.usuario()).toEqual(usuario);
    });
  it('encerra a autenticação apenas quando a API retorna 401', async () => {
    get.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));
    const auth = TestBed.inject(AuthService);
    auth.usuario.set(usuario);
    expect(await auth.restore()).toBeNull();
    expect(auth.usuario()).toBeNull();
  });
  it('restaura a sessão quando a API volta a responder', async () => {
    get.mockReturnValue(of(usuario));
    const auth = TestBed.inject(AuthService);
    expect(await auth.restore()).toEqual(usuario);
  });
});

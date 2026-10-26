import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { EditarUsuarioComponent } from './editar';

describe('Edição de usuário', () => {
  it('carrega o registro e envia senha nula para preservar a senha atual', async () => {
    const get = vi.fn().mockResolvedValue({ login: 'usuario.atual' });
    const put = vi.fn().mockResolvedValue({ id: 2 });
    await TestBed.configureTestingModule({ imports: [EditarUsuarioComponent], providers: [
      provideRouter([]), { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '2' }) } } },
      { provide: ApiService, useValue: { get, put } },
      { provide: AuthService, useValue: { usuario: signal({ id: 1 }) } },
    ] }).compileComponents();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(EditarUsuarioComponent);
    await fixture.whenStable();
    expect(get).toHaveBeenCalledWith('/administradores/2');
    const input = fixture.nativeElement.querySelector('#editar-login') as HTMLInputElement;
    input.value = 'usuario.novo'; input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(put).toHaveBeenCalledWith('/administradores/2', { login: 'usuario.novo', senha: null });
    expect(navigate).toHaveBeenCalledWith('/cadastro/usuarios/listar');
  });
});

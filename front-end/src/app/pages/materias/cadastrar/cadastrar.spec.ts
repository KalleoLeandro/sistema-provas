import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { CadastrarMateriaComponent } from './cadastrar';

describe('Validação do cadastro de matéria', () => {
  const post = vi.fn(); const navigateByUrl = vi.fn();
  beforeEach(() => {
    post.mockReset().mockResolvedValue({ id: 1 }); navigateByUrl.mockReset().mockResolvedValue(true);
    TestBed.configureTestingModule({ providers: [{ provide: ApiService, useValue: { post } }, { provide: Router, useValue: { navigateByUrl } }] });
  });

  it('destaca nome vazio ou composto por espaços e libera após corrigir', async () => {
    const fixture = TestBed.createComponent(CadastrarMateriaComponent); await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeNull();
    input.dispatchEvent(new Event('blur')); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#materia-nome-erro').textContent).toContain('obrigatório');
    input.value = '   '; input.dispatchEvent(new Event('input')); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#materia-nome-erro').textContent).toContain('apenas espaços');
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    expect(post).not.toHaveBeenCalled();
    input.value = ' Matemática '; input.dispatchEvent(new Event('input')); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#materia-nome-erro')).toBeNull();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })); await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/materias', { nome: 'Matemática' });
    expect(navigateByUrl).toHaveBeenCalledWith('/cadastro/materias/listar');
  });

  it('limita o nome a 120 caracteres e limpar remove o estado de erro', async () => {
    const fixture = TestBed.createComponent(CadastrarMateriaComponent); await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'A'.repeat(121); input.dispatchEvent(new Event('input')); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#materia-nome-erro').textContent).toContain('120 caracteres');
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    (fixture.nativeElement.querySelector('[variant=secondary]') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(input.value).toBe(''); expect(fixture.nativeElement.querySelector('#materia-nome-erro')).toBeNull();
    expect(post).not.toHaveBeenCalled();
  });
});

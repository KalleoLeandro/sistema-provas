import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { EditarMateriaComponent } from './editar';
describe('Edição de matéria', () => {
  const get = vi.fn();
  const put = vi.fn();
  beforeEach(() => {
    get.mockReset().mockResolvedValue({ id: 3, nome: 'Matemática' });
    put.mockReset().mockResolvedValue({ id: 3, nome: 'História' });
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: '3' }) } },
        },
        { provide: ApiService, useValue: { get, put } },
      ],
    });
  });
  it('carrega e salva o nome, retornando à lista', async () => {
    const navigation = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(EditarMateriaComponent);
    await fixture.whenStable();
    expect(get).toHaveBeenCalledWith('/materias/3');
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    expect(input.value).toBe('Matemática');
    input.value = '  História  ';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    await fixture.whenStable();
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(put).toHaveBeenCalledWith('/materias/3', { nome: 'História' });
    expect(navigation).toHaveBeenCalledWith('/cadastro/materias/listar');
  });
  it('não permite salvar quando a matéria não foi carregada', async () => {
    get.mockRejectedValueOnce(new Error('Não encontrada'));
    const fixture = TestBed.createComponent(EditarMateriaComponent);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
    expect(put).not.toHaveBeenCalled();
  });
});

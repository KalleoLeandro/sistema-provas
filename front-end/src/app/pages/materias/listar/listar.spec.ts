import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { ListarMateriasComponent } from './listar';

describe('Lista de matérias', () => {
  const get = vi.fn();
  const remove = vi.fn();
  beforeEach(() => {
    remove.mockReset().mockResolvedValue(undefined);
    get.mockReset().mockResolvedValue([
      { id: 1, nome: 'História' },
      { id: 2, nome: 'Matemática' },
    ]);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: ApiService, useValue: { get, delete: remove } }],
    });
  });
  it('lista dados da API e pesquisa sem depender de acentos', async () => {
    const fixture = TestBed.createComponent(ListarMateriasComponent);
    await fixture.whenStable();
    expect(get).toHaveBeenCalledWith('/materias');
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(2);
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = '  matematica  ';
    input.dispatchEvent(new Event('input', { bubbles: true }));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Matemática');
    expect(fixture.nativeElement.querySelector('a').getAttribute('href')).toBe(
      '/cadastro/materias/cadastrar',
    );
  });
  it('distingue lista vazia de pesquisa sem resultados', async () => {
    get.mockResolvedValueOnce([]);
    const fixture = TestBed.createComponent(ListarMateriasComponent);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Nenhuma matéria cadastrada.');
    fixture.componentInstance.materias.set([{ id: 1, nome: 'História' }]);
    fixture.componentInstance.search.set('inexistente');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain(
      'Nenhuma matéria encontrada para esta pesquisa.',
    );
  });
  it('permite tentar novamente após falha sem exibir lista vazia como sucesso', async () => {
    get.mockRejectedValueOnce(new Error('Falha de rede'));
    const fixture = TestBed.createComponent(ListarMateriasComponent);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Não foi possível carregar',
    );
    expect(fixture.nativeElement.querySelector('table')).toBeNull();
    [...fixture.nativeElement.querySelectorAll('button')]
      .find((button: any) => button.textContent.includes('Tentar novamente'))
      .click();
    await fixture.whenStable();
    expect(get).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.querySelector('table')).not.toBeNull();
  });
  it('confirma antes de excluir e atualiza a lista após sucesso', async () => {
    const fixture = TestBed.createComponent(ListarMateriasComponent);
    await fixture.whenStable();
    fixture.componentInstance.confirmDelete({ id: 1, nome: 'História' });
    await fixture.whenStable();
    expect(remove).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('dialog').hasAttribute('open')).toBe(true);
    fixture.componentInstance.closeDelete();
    expect(remove).not.toHaveBeenCalled();
    fixture.componentInstance.confirmDelete({ id: 1, nome: 'História' });
    await fixture.componentInstance.remove();
    await fixture.whenStable();
    expect(remove).toHaveBeenCalledWith('/materias/1');
    expect(fixture.componentInstance.materias().map((item) => item.id)).toEqual([2]);
    expect(fixture.componentInstance.deleteTarget()).toBeNull();
  });
  it('preserva registro e confirmação quando a exclusão falha', async () => {
    remove.mockRejectedValueOnce(new Error('Falha'));
    const fixture = TestBed.createComponent(ListarMateriasComponent);
    await fixture.whenStable();
    fixture.componentInstance.confirmDelete({ id: 1, nome: 'História' });
    await fixture.componentInstance.remove();
    await fixture.whenStable();
    expect(fixture.componentInstance.materias()).toHaveLength(2);
    expect(fixture.componentInstance.deleteTarget()?.id).toBe(1);
    expect(fixture.componentInstance.actionError()).toContain('Não foi possível excluir');
  });
});

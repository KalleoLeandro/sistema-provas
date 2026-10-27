import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { ListarUsuariosComponent } from './listar';

describe('Status dos administradores', () => {
  const get = vi.fn(); const patch = vi.fn();
  it('permite filtrar os perfis sem mostrar administradores como professores', async () => {
    const fixture = TestBed.createComponent(ListarUsuariosComponent);
    await fixture.whenStable();
    expect(fixture.componentInstance.usuariosFiltrados().length).toBe(1);
    expect(fixture.componentInstance.perfil()).toBe('administrador');
    expect(fixture.nativeElement.querySelector('option[value="todos"]')).toBeNull();
    await fixture.componentInstance.changeProfile('professor');
    await fixture.whenStable();
    expect(fixture.componentInstance.usuariosFiltrados().length).toBe(0);
    expect(fixture.nativeElement.textContent).toContain('Nenhum professor cadastrado.');
    expect(fixture.nativeElement.textContent).not.toContain('Outro admin');
  });
  beforeEach(async () => {
    get.mockReset(); patch.mockReset();
    get.mockImplementation(async (path: string) => path === '/administradores' ? [{ id: 2, nome: 'Outro admin', login: 'outro', ativo: true }] : []);
    await TestBed.configureTestingModule({ imports: [ListarUsuariosComponent], providers: [
      provideRouter([]), { provide: ApiService, useValue: { get, patch } },
      { provide: AuthService, useValue: { usuario: signal({ id: 1 }) } },
    ] }).compileComponents();
  });

  it('exibe ativo e permite desativar e reativar pela lista', async () => {
    const fixture = TestBed.createComponent(ListarUsuariosComponent);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.status-visual')?.textContent).toBe('Ativo');
    patch.mockResolvedValue({ id: 2, nome: 'Outro admin', login: 'outro', ativo: false });
    (element.querySelector('td button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(patch).toHaveBeenCalledWith('/administradores/2/status', { ativo: false });
    expect(element.querySelector('.status-visual')?.textContent).toBe('Inativo');
    patch.mockResolvedValue({ id: 2, nome: 'Outro admin', login: 'outro', ativo: true });
    (element.querySelector('td button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(patch).toHaveBeenCalledWith('/administradores/2/status', { ativo: true });
    expect(element.querySelector('.status-visual')?.textContent).toBe('Ativo');
  });

  it('mantém o status anterior quando a alteração falha', async () => {
    patch.mockRejectedValue(new Error('Falha'));
    const fixture = TestBed.createComponent(ListarUsuariosComponent);
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('td button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.componentInstance.usuarios()[0].ativo).toBe(true);
    expect(fixture.componentInstance.erro()).toContain('Não foi possível');
  });

  it('mostra turmas de professores e permite desativar o perfil sem alterar administradores', async () => {
    const professor = { id: 5, nome: 'Maria Silva', login: 'maria.prof', ativo: true,
      turmas: [{ id: 2, ensino: 'M', ano: 2, letra: 'A' }] };
    const fixture = TestBed.createComponent(ListarUsuariosComponent);
    await fixture.whenStable();
    get.mockResolvedValueOnce([professor]);
    await fixture.componentInstance.changeProfile('professor');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Maria Silva');
    expect(fixture.nativeElement.textContent).toContain('2º ano A');
    patch.mockResolvedValue({ ...professor, ativo: false });
    (fixture.nativeElement.querySelector('td button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(patch).toHaveBeenCalledWith('/professores/5/status', { ativo: false });
    expect(fixture.nativeElement.querySelector('.status-visual')?.textContent).toBe('Inativo');
  });
  it('lista alunos com número opcional e situação escolar M/T',async()=>{
    const fixture=TestBed.createComponent(ListarUsuariosComponent);await fixture.whenStable();
    get.mockResolvedValueOnce({itens:[{id:1,nome:'Aluno exemplo',numero:null,cgm:'00123',status:'T',turmaId:5,ensino:'F',ano:1,letra:'A'}],total:1,pagina:1,tamanho:15});
    await fixture.componentInstance.changeProfile('aluno');await fixture.whenStable();
    expect(get).toHaveBeenLastCalledWith('/alunos?pagina=1&tamanho=15');
    expect(fixture.nativeElement.textContent).toContain('Transferido');expect(fixture.nativeElement.textContent).toContain('00123');
    expect(fixture.nativeElement.querySelector('visual-pagination')).toBeTruthy();
  });


  it('combina nome sem acento, status e turma na lista completa de professores',async()=>{
    const f=TestBed.createComponent(ListarUsuariosComponent);await f.whenStable();const c=f.componentInstance;
    c.professores.set([{id:1,nome:'João',login:'joao',ativo:true,turmas:[],materias:[{materiaId:1,nome:'Matemática',turmas:[{id:5,ensino:'F',ano:1,letra:'A'}]}]},{id:2,nome:'João',login:'outro',ativo:false,turmas:[],materias:[]}]);
    c.busca.set('joao');c.filtroStatus.set('ativo');c.filtroTurma.set(5);expect(c.professoresFiltrados().map(p=>p.id)).toEqual([1]);
    c.filtroStatus.set('inativo');expect(c.professoresFiltrados()).toEqual([]);
  });
  it('envia os filtros de alunos antes de paginar e exibe remanejado',async()=>{
    const f=TestBed.createComponent(ListarUsuariosComponent);await f.whenStable();const c=f.componentInstance;
    await c.changeProfile('aluno');c.busca.set('Ana');c.filtroStatus.set('R');c.filtroTurma.set(5);
    get.mockResolvedValueOnce({itens:[{id:4,nome:'Ana',numero:null,cgm:'001',status:'R',turmaId:5,ensino:'F',ano:1,letra:'A'}],total:1,pagina:1,tamanho:15});
    await c.changeProfile('aluno',2);f.detectChanges();expect(get).toHaveBeenLastCalledWith('/alunos?pagina=2&tamanho=15&busca=Ana&status=R&turmaId=5');expect(f.nativeElement.textContent).toContain('Remanejado');
  });
});

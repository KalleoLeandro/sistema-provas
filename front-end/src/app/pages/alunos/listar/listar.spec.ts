import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { ListarAlunosComponent } from './listar';
describe('Consulta de alunos do professor', () => {
  const get=vi.fn();
  beforeEach(()=>{
    get.mockReset().mockImplementation((path:string)=>Promise.resolve(path==='/professor/dashboard'
      ? {materias:[{turmas:[{id:5,ensino:'F',ano:1,letra:'A'}]}]}
      : {itens:[{id:1,nome:'Aluno exemplo',numero:null,cgm:'123',status:'M',turmaId:5,ensino:'F',ano:1,letra:'A'}],total:60,pagina:1,tamanho:15}));
    TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:{get}}]});
  });
  it('carrega a consulta com 15 e envia turma e tamanho na mudança de filtro', async()=>{
    const f=TestBed.createComponent(ListarAlunosComponent); await f.whenStable();
    await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).toContain('Aluno exemplo');});
    expect(get).toHaveBeenCalledWith('/professor/alunos?pagina=1&tamanho=15');
    const c=f.componentInstance; c.pagina=3; c.turmaId=5; c.changeSize(25); await f.whenStable();
    expect(get).toHaveBeenLastCalledWith('/professor/alunos?pagina=1&tamanho=25&turmaId=5');
    c.changePage(2); await f.whenStable();
    expect(get).toHaveBeenLastCalledWith('/professor/alunos?pagina=2&tamanho=25&turmaId=5');
    c.changeTurma(); await f.whenStable();expect(c.pagina).toBe(1);
  });
  it('mostra erro em falha da API em vez de alegar lista vazia', async()=>{
    get.mockRejectedValue(new Error('Falha'));
    const f=TestBed.createComponent(ListarAlunosComponent);await f.whenStable();
    expect(f.nativeElement.textContent).toContain('Não foi possível carregar');
    expect(f.nativeElement.textContent).not.toContain('Nenhum aluno encontrado');
  });
});

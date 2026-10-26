import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { ProvasAlunoComponent } from './provas';
describe('Provas disponíveis do aluno',()=>{
  const get=vi.fn();
  beforeEach(()=>{get.mockReset().mockResolvedValue({provas:{itens:[{id:1,titulo:'Avaliação de matemática',materia:'Matemática',professor:'Maria',disponivelEm:'2030-01-01T12:00:00Z',encerraEm:'2030-01-02T12:00:00Z'}],total:20,pagina:1,tamanho:15},total:20,concluidas:0});TestBed.configureTestingModule({providers:[provideRouter([]),{provide:ApiService,useValue:{get}}]});});
  it('mostra lista e progresso com total de todas as páginas',async()=>{
    const f=TestBed.createComponent(ProvasAlunoComponent);await f.whenStable();expect(f.nativeElement.textContent).toContain('Avaliação de matemática');expect(f.nativeElement.textContent).toContain('0 de 20 provas concluídas');
    const progress=f.nativeElement.querySelector('progress') as HTMLProgressElement;expect(progress.max).toBe(20);expect(progress.value).toBe(0);
    f.componentInstance.changeSize(25);await f.whenStable();expect(get).toHaveBeenLastCalledWith('/aluno/provas?pagina=1&tamanho=25');
  });
  it('diferencia falha de consulta de lista vazia',async()=>{
    get.mockRejectedValue(new Error());const f=TestBed.createComponent(ProvasAlunoComponent);await f.whenStable();expect(f.nativeElement.textContent).toContain('Não foi possível carregar');expect(f.nativeElement.textContent).not.toContain('não possui provas');
  });
  it('lista vazia mantém progresso em zero',async()=>{
    get.mockResolvedValue({provas:{itens:[],total:0,pagina:1,tamanho:15},total:0,concluidas:0});const f=TestBed.createComponent(ProvasAlunoComponent);await f.whenStable();expect(f.nativeElement.textContent).toContain('Você não possui provas disponíveis');expect(f.nativeElement.querySelector('progress').value).toBe(0);
  });
});

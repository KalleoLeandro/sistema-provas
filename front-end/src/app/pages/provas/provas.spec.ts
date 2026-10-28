import { TestBed } from '@angular/core/testing';
import { NgForm } from '@angular/forms';
import { provideHttpClient } from '@angular/common/http';
import { vi } from 'vitest';
import { ApiService } from '../../services/api.service';
import { provideDesignSystem } from '../../design-system/providers';
import { ProvasComponent } from './provas';
describe('Consulta e edição de liberações',()=>{
 const get=vi.fn(),post=vi.fn(),put=vi.fn();
 const row={assuntos:[{id:1,materiaId:6,nome:'Frações'}],id:11,titulo:'Título da lista',materiaId:6,materia:'Matemática',duracaoMinutos:60,tokenConfigurado:true,disponivelEm:'2099-01-01T11:00:00Z',encerraEm:'2099-01-01T13:00:00Z',situacao:'agendada' as const};
 const stock=[{id:1,materiaId:6,nome:'Frações',completo:true,quantidades:[{peso:0.5,minimo:4,cadastradas:4,faltantes:0}]}];
 const detail={...row,titulo:'Título atual',token:'TokenAtual',precisaNovoToken:false};
 beforeEach(()=>{
  get.mockReset().mockImplementation((path:string)=>Promise.resolve(path==='/professor/dashboard'?{materias:[{materiaId:6,nome:'Matemática',turmas:[{id:5,ensino:'F',ano:1,letra:'A'}]}]}:path.endsWith('/liberacoes/11')?detail:path.includes('/assuntos?')?stock:path.startsWith('/professor/provas/')?{itens:[row],total:1,pagina:1,tamanho:15}:{itens:[],total:0,pagina:1,tamanho:15}));post.mockReset().mockResolvedValue({});put.mockReset().mockResolvedValue({});
  TestBed.configureTestingModule({providers:[provideHttpClient(),provideDesignSystem(),{provide:ApiService,useValue:{get,post,put}}]});
 });
 async function choose(){const f=TestBed.createComponent(ProvasComponent);await f.whenStable();const input=f.nativeElement.querySelector('#provas-turma-liberacao') as HTMLSelectElement;input.selectedIndex=1;input.dispatchEvent(new Event('change',{bubbles:true}));await f.whenStable();return f;}
 it('consulta a lista sem abrir automaticamente o formulário',async()=>{const f=await choose();expect(f.nativeElement.textContent).toContain('Título da lista');expect(f.nativeElement.querySelector('.exam-release-form')).toBeNull();});
 it('reconsulta o registro e preenche título, datas, duração e token visível',async()=>{
  const f=await choose();const button=Array.from(f.nativeElement.querySelectorAll('button')).find((b:any)=>b.textContent.trim()==='Editar') as HTMLButtonElement;button.click();await f.whenStable();
  expect(get).toHaveBeenCalledWith('/professor/provas/turmas/5/liberacoes/11');expect(f.nativeElement.querySelector('#prova-titulo').value).toBe('Título atual');expect(f.nativeElement.querySelector('#prova-token').value).toBe('TokenAtual');expect(f.nativeElement.querySelector('#prova-token').type).toBe('text');expect(f.nativeElement.querySelectorAll('visual-date-field').length).toBe(2);expect(f.nativeElement.querySelector('input[type=datetime-local]')).toBeNull();expect(f.componentInstance.disponivelEm).toBe(detail.disponivelEm);expect(f.componentInstance.encerraEm).toBe(detail.encerraEm);
 });
 it('abre cadastro somente por ação explícita e preserva o prazo UTC no envio',async()=>{
  const f=await choose();const c=f.componentInstance;c.newRelease();await f.whenStable();c.assuntoIds=[1];c.titulo='Nova prova';c.disponivelEm=detail.disponivelEm;c.encerraEm=detail.encerraEm;c.token='TokenNovo';
  const form={control:{markAllAsTouched:vi.fn()},invalid:false,resetForm:vi.fn()} as unknown as NgForm;await c.release(form);
  expect(post).toHaveBeenCalledWith('/professor/provas/turmas/5/liberacoes',expect.objectContaining({titulo:'Nova prova',disponivelEm:new Date(detail.disponivelEm).toISOString(),encerraEm:new Date(detail.encerraEm).toISOString(),token:'TokenNovo'}));expect(c.formOpen()).toBe(false);expect(c.token).toBe('');
 });
 it('cancelar a edição limpa campos e mantém a lista',async()=>{const f=await choose();const c=f.componentInstance;await c.edit(row);c.cancelEditor();expect(c.token).toBe('');expect(c.formOpen()).toBe(false);expect(c.provas()).toHaveLength(1);});
 it('falha na consulta de detalhes não abre formulário vazio',async()=>{const f=await choose();get.mockRejectedValue({error:{mensagem:'Falha de leitura'}});await f.componentInstance.edit(row);expect(f.componentInstance.formOpen()).toBe(false);expect(f.componentInstance.erroProvas()).toBe('Falha de leitura');});
});

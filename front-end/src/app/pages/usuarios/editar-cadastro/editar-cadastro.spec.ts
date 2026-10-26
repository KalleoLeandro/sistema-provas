import {TestBed} from '@angular/core/testing';
import {ActivatedRoute,Router,convertToParamMap,provideRouter} from '@angular/router';
import {NgForm} from '@angular/forms';
import {vi} from 'vitest';
import {ApiService} from '../../../services/api.service';
import {EditarCadastroComponent} from './editar-cadastro';
describe('Edição administrativa de alunos e professores',()=>{
 const get=vi.fn(),put=vi.fn();
 const turma={id:5,ensino:'F',ano:8,letra:'C'};
 const professor={id:7,nome:'Mariana',login:'mariana',ativo:false,materias:[{materiaId:6,nome:'Matemática',turmas:[turma]}],turmas:[turma]};
 const aluno={id:7,nome:'Ana',numero:null,cgm:'00123',status:'T',turmaId:5};
 function setup(perfil:string){get.mockImplementation((p:string)=>Promise.resolve(p==='/turmas'?[turma]:p==='/materias'?[{id:6,nome:'Matemática'}]:perfil==='professor'?professor:aluno));TestBed.configureTestingModule({providers:[provideRouter([]),{provide:ApiService,useValue:{get,put}},{provide:ActivatedRoute,useValue:{snapshot:{data:{perfil},paramMap:convertToParamMap({id:'7'})}}}]});vi.spyOn(TestBed.inject(Router),'navigate').mockResolvedValue(true);return TestBed.createComponent(EditarCadastroComponent);}
 beforeEach(()=>{get.mockReset();put.mockReset().mockResolvedValue({});});
 it('preenche as atribuições e mantém a senha opcional sem alterar o status',async()=>{
  const f=setup('professor');await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.componentInstance.loaded()).toBe(true);});const c=f.componentInstance;expect(c.nome).toBe('Mariana');expect(c.materias).toEqual([6]);expect(c.turmasPorMateria[6]).toEqual([5]);expect(c.senha).toBe('');expect(f.nativeElement.querySelector('#editar-professor-senha').required).toBe(false);c.nome='Mariana Costa';await c.save({control:{markAllAsTouched:vi.fn()},invalid:false} as unknown as NgForm);expect(put).toHaveBeenCalledWith('/professores/7',{nome:'Mariana Costa',login:'mariana',senha:null,materias:[{materiaId:6,turmas:[5]}]});expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/cadastro/usuarios/listar'],{queryParams:{perfil:'professor'}});
 });
 it('preenche aluno preservando zeros do CGM e aceita número opcional',async()=>{
  const f=setup('aluno');await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.componentInstance.loaded()).toBe(true);});const c=f.componentInstance;expect(c.cgm).toBe('00123');expect(c.numero).toBeNull();expect(c.status).toBe('T');await c.save({control:{markAllAsTouched:vi.fn()},invalid:false} as unknown as NgForm);expect(put).toHaveBeenCalledWith('/alunos/7',{nome:'Ana',numero:null,cgm:'00123',status:'T',turmaId:5});
 });
 it('mantém o formulário aberto e os vínculos quando o servidor rejeita conflito',async()=>{
  const f=setup('professor');await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.componentInstance.loaded()).toBe(true);});put.mockRejectedValue({error:{mensagem:'Matéria e turma ocupadas'}});await f.componentInstance.save({control:{markAllAsTouched:vi.fn()},invalid:false} as unknown as NgForm);expect(f.componentInstance.erro()).toContain('ocupadas');expect(f.componentInstance.turmasPorMateria[6]).toEqual([5]);expect(TestBed.inject(Router).navigate).not.toHaveBeenCalled();
 });
 it('impede salvar formulário inválido e limpa dados ao sair',async()=>{
  const f=setup('aluno');await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.componentInstance.loaded()).toBe(true);});await f.componentInstance.save({control:{markAllAsTouched:vi.fn()},invalid:true} as unknown as NgForm);expect(put).not.toHaveBeenCalled();f.componentInstance.clear();expect(f.componentInstance.cgm).toBe('');expect(f.componentInstance.nome).toBe('');expect(f.componentInstance.loaded()).toBe(false);
 });
});

import {TestBed} from '@angular/core/testing';
import {vi} from 'vitest';
import {ApiService} from '../../services/api.service';
import {PerfilProfessorComponent} from './perfil';
describe('Perfil do professor',()=>{
 it('consulta a identidade da sessão e apresenta as matérias e turmas somente para leitura',async()=>{
  const get=vi.fn().mockResolvedValue({id:1,nome:'Mariana Costa',login:'demo.matematica.c',ativo:true,materias:[{materiaId:6,nome:'Matemática',turmas:[{id:1,ensino:'F',ano:8,letra:'C'}]}]});
  TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:{get}}]});const f=TestBed.createComponent(PerfilProfessorComponent);await f.whenStable();
  expect(get).toHaveBeenCalledWith('/professor/dashboard');expect(f.nativeElement.textContent).toContain('Mariana Costa');expect(f.nativeElement.textContent).toContain('8º ano C');expect(f.nativeElement.querySelector('input')).toBeNull();
 });
});

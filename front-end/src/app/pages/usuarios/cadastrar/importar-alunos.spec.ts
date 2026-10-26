import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { ImportarAlunosComponent } from './importar-alunos';
const csv='nome;numero;cgm;status;ensino;ano;letra\nAna;;00123;M;F;1;A\n';
const valid={total:1,validos:1,importados:0,erros:[],linhas:[{linha:2,nome:'Ana',numero:null,cgm:'00123',status:'M',turmaId:5,ensino:'F',ano:1,letra:'A'}]};
describe('Importação administrativa de alunos por CSV',()=>{
  const post=vi.fn();
  beforeEach(()=>{post.mockReset();TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:{post}}]});});
  async function setup(){
    const f=TestBed.createComponent(ImportarAlunosComponent);await f.whenStable();
    const event={target:{files:[{name:'alunos.csv',size:csv.length,text:()=>Promise.resolve(csv)}]}} as unknown as Event;
    await f.componentInstance.selectFile(event);await f.whenStable();return f;
  }
  it('exige a prévia e limpa o arquivo após importar, sem permitir envio repetido',async()=>{
    const f=await setup();const c=f.componentInstance;expect(c.podeImportar).toBe(false);
    post.mockResolvedValueOnce(valid);await c.validate();await f.whenStable();
    expect(post).toHaveBeenCalledWith('/alunos/importar/validar',{csv});expect(c.podeImportar).toBe(true);
    post.mockResolvedValueOnce({...valid,importados:1});await c.import();await f.whenStable();
    expect(post).toHaveBeenLastCalledWith('/alunos/importar',{csv});expect(c.hasFile).toBe(false);
    expect(f.nativeElement.textContent).toContain('1 aluno(s) importado(s) com sucesso.');
    await c.import();expect(post).toHaveBeenCalledTimes(2);
  });
  it('mostra erros por linha e impede importar o arquivo com qualquer falha',async()=>{
    const f=await setup();const c=f.componentInstance;
    post.mockResolvedValueOnce({...valid,erros:[{linha:3,campo:'cgm',mensagem:'CGM já cadastrado.'}]});
    await c.validate();await f.whenStable();expect(c.podeImportar).toBe(false);
    expect(f.nativeElement.textContent).toContain('CGM já cadastrado.');
    expect(f.nativeElement.textContent).toContain('Nenhum aluno foi importado.');
    await c.import();expect(post).toHaveBeenCalledTimes(1);
  });
  it('trocar de arquivo invalida a validação anterior',async()=>{
    const f=await setup();const c=f.componentInstance;post.mockResolvedValueOnce(valid);await c.validate();
    await c.selectFile({target:{files:[{name:'outro.csv',size:csv.length,text:()=>Promise.resolve(csv)}]}} as unknown as Event);
    expect(c.resultado()).toBeNull();expect(c.podeImportar).toBe(false);
  });
});

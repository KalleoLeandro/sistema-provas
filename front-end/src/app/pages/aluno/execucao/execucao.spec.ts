import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter,Router,ActivatedRoute,convertToParamMap } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { ExecucaoProvaComponent } from './execucao';
import { TentativaProva } from '../../../models/execucao-prova';
describe('Execução da prova do aluno',()=>{
 const get=vi.fn(),post=vi.fn(),put=vi.fn(),logout=vi.fn();
 let attempt:TentativaProva;
 beforeEach(()=>{
  attempt={liberacaoId:5,titulo:'Avaliação',materia:'Matemática',iniciadaEm:new Date().toISOString(),encerraEm:new Date(Date.now()+3600000).toISOString(),agora:new Date().toISOString(),situacao:'em_andamento',enviadaAgora:false,questoes:[{numero:1,tipo:'objetiva',peso:0.5,enunciado:'Calcule',alternativas:{A:'1',B:'2'},resposta:null,revisao:0},{numero:2,tipo:'discursiva',peso:1,enunciado:'Explique',alternativas:{},resposta:null,revisao:0}]};
  get.mockReset().mockResolvedValue({id:5,titulo:'Avaliação',materia:'Matemática',duracaoMinutos:60,encerraEm:attempt.encerraEm,iniciada:false,submetida:false});post.mockReset().mockResolvedValue(attempt);put.mockReset().mockImplementation(()=>Promise.resolve({...attempt,questoes:attempt.questoes.map(q=>q.numero===1?{...q,resposta:'A',revisao:1}:q)}));logout.mockReset().mockResolvedValue(undefined);
  TestBed.configureTestingModule({providers:[provideRouter([]),{provide:ActivatedRoute,useValue:{snapshot:{paramMap:convertToParamMap({id:'5'})}}},{provide:ApiService,useValue:{get,post,put}},{provide:AuthService,useValue:{logout,usuario:signal({perfil:'aluno'})}}]});vi.spyOn(TestBed.inject(Router),'navigateByUrl').mockResolvedValue(true);
 });
 it('exige aceite e token antes de iniciar e salva cada alternativa com revisão',async()=>{
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();const c=f.componentInstance;
  await c.start();expect(post).not.toHaveBeenCalled();c.token='Token123';c.accepted=true;await c.start();f.detectChanges();
  expect(post).toHaveBeenCalledWith('/aluno/provas/5/iniciar',{token:'Token123',aceitouRegras:true});expect(c.token).toBe('');
  c.answers[1]='A';c.change(1,true);await vi.waitFor(()=>expect(put).toHaveBeenCalledWith('/aluno/provas/5/questoes/1/resposta',{resposta:'A',revisao:0}));await c.flush();expect(c.pendingCount()).toBe(0);
 });
 it('confirma questões em branco e encerra a sessão depois da entrega',async()=>{
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();const c=f.componentInstance;c.token='Token123';c.accepted=true;await c.start();await c.requestSubmit();expect(c.confirmOpen()).toBe(true);expect(c.missing).toBe(2);
  post.mockResolvedValue({...attempt,situacao:'submetida',enviadaAgora:true});await c.submit(true);expect(post).toHaveBeenLastCalledWith('/aluno/provas/5/submeter',{confirmarIncompleta:true});expect(c.deliveryOpen()).toBe(true);expect(logout).not.toHaveBeenCalled();expect(TestBed.inject(Router).navigateByUrl).not.toHaveBeenCalled();await c.acknowledgeDelivery();expect(logout).toHaveBeenCalled();expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/login');
 });
 it('preserva a resposta pendente e impede navegação se o salvamento falhar',async()=>{
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();const c=f.componentInstance;c.token='Token123';c.accepted=true;await c.start();put.mockRejectedValue({error:{mensagem:'Redis indisponível'}});c.answers[1]='B';c.change(1,true);await c.flush();expect(c.answers[1]).toBe('B');expect(c.pendingCount()).toBe(1);expect(await c.tryLeave()).toBe(false);
 });
 it('retoma a cópia e mostra respostas entregues sem nota ou gabarito',async()=>{
  get.mockImplementation((path:string)=>Promise.resolve(path.endsWith('/regras')?{iniciada:true}:{...attempt,situacao:'submetida',questoes:attempt.questoes.map(q=>({...q,resposta:q.numero===1?'A':null}))}));
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();expect(f.componentInstance.readonly).toBe(true);expect(f.componentInstance.answers[1]).toBe('A');expect(f.nativeElement.textContent).toContain('Não respondida.');expect(logout).not.toHaveBeenCalled();expect(post).not.toHaveBeenCalled();
 });
 it('também pede confirmação quando todas as questões foram respondidas',async()=>{
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();const c=f.componentInstance;c.token='Token123';c.accepted=true;await c.start();c.answers[1]='A';c.answers[2]='Resposta';post.mockClear();await c.requestSubmit();expect(c.missing).toBe(0);expect(c.confirmOpen()).toBe(true);expect(post).not.toHaveBeenCalled();c.confirmOpen.set(false);expect(c.attempt()?.situacao).toBe('em_andamento');
 });
 for(const action of ['ok','close','escape'])it('redireciona somente ao reconhecer a entrega por '+action,async()=>{
  const f=TestBed.createComponent(ExecucaoProvaComponent);await f.whenStable();const c=f.componentInstance;c.token='Token123';c.accepted=true;await c.start();post.mockResolvedValue({...attempt,situacao:'submetida',enviadaAgora:true});await c.submit(true);await f.whenStable();
  const dialog=f.nativeElement.querySelector('dialog.with-close') as HTMLDialogElement;expect(dialog.textContent).toContain('Aguarde a correção do professor');expect(TestBed.inject(Router).navigateByUrl).not.toHaveBeenCalled();
  if(action==='escape')dialog.dispatchEvent(new Event('cancel',{cancelable:true}));else if(action==='close')(dialog.querySelector('[aria-label="Fechar"]') as HTMLButtonElement).click();else (Array.from(dialog.querySelectorAll('button')).find(b=>b.textContent?.trim()==='OK') as HTMLButtonElement).click();
  await f.whenStable();expect(logout).toHaveBeenCalledTimes(1);expect(TestBed.inject(Router).navigateByUrl).toHaveBeenCalledWith('/login');
 });
});

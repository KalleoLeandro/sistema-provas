import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ApiService } from '../../services/api.service';
import { RelatoriosComponent } from './relatorios';

describe('Relatórios configuráveis',()=>{
 const get=vi.fn();
 beforeEach(()=>{get.mockReset().mockResolvedValue([]);TestBed.configureTestingModule({providers:[{provide:ApiService,useValue:{get}}]});});
 it('exporta somente campos escolhidos, escapando separador, aspas e fórmulas',()=>{
  const c=TestBed.createComponent(RelatoriosComponent).componentInstance;c.selecionados={nome:true,cgm:true};
  c.alunos.set([{id:1,nome:'=SOMA(1;2) "Aluno"',numero:null,cgm:'001',status:'M',turmaId:5,ensino:'F',ano:1,letra:'A'}]);
  expect(c.csv()).toBe('\ufeff"Aluno";"CGM"\r\n"\'=SOMA(1;2) ""Aluno""";"001"');
 });
 it('mantém nota parcial identificada mesmo sem selecionar a coluna situação',()=>{
  const c=TestBed.createComponent(RelatoriosComponent).componentInstance;c.modo='resultados';c.selecionados={nota:true};
  c.resultados.set([{alunoId:1,nome:'Ana',numero:1,cgm:'001',status:'M',tentativaId:4,iniciadaEm:null,entregueEm:'2026-10-25T12:00:00Z',pendentes:2,nota:4.5}]);
  expect(c.csv()).toContain('4,50 (parcial)');
 });
 it('descarta consulta anterior ao trocar de turma',async()=>{
  const c=TestBed.createComponent(RelatoriosComponent).componentInstance;let resolve!:(v:unknown[])=>void;
  get.mockReturnValueOnce(new Promise(r=>resolve=r));c.turmaId=5;const old=c.changeClass();
  c.turmaId=null;await c.changeClass();resolve([{nome:'Aluno de outra turma'}]);await old;
  expect(c.alunos()).toEqual([]);expect(c.carregado()).toBe(false);
 });
 it('impede exportação sem campo selecionado',()=>{
  const c=TestBed.createComponent(RelatoriosComponent).componentInstance;c.carregado.set(true);c.selecionados={};
  const print=vi.spyOn(window,'print').mockImplementation(()=>{});c.export();expect(print).not.toHaveBeenCalled();print.mockRestore();
 });
});

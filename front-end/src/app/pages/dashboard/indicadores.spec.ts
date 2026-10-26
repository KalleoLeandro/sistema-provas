import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../services/api.service';
import { DashboardIndicadoresComponent } from './indicadores';

describe('Indicadores do dashboard',()=>{
 const get=vi.fn();
 beforeEach(()=>{get.mockReset();TestBed.configureTestingModule({providers:[provideRouter([]),{provide:ApiService,useValue:{get}}]});});
 it('mostra ausência de média e contagens zeradas sem criar notas fictícias',async()=>{
  get.mockResolvedValue({alunosFizeram:0,alunosPendentes:0,mediaGeral:null,naoIniciadas:0,emAndamento:0,aguardandoCorrecao:0,corrigidas:0,mediasTurmas:[]});
  const f=TestBed.createComponent(DashboardIndicadoresComponent);await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).not.toContain('Carregando indicadores');});
  expect([...f.nativeElement.querySelectorAll('.metric-card-value')].map((e:any)=>e.textContent)).toEqual(['0','0','—']);expect(f.nativeElement.querySelector('canvas')).toBeNull();expect(get).toHaveBeenCalledWith('/dashboard/indicadores');
 });
 it('não apresenta falha de consulta como indicadores zerados',async()=>{
  get.mockRejectedValue(new Error('Indisponível'));const f=TestBed.createComponent(DashboardIndicadoresComponent);await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).not.toContain('Carregando indicadores');});expect(f.nativeElement.textContent).toContain('Não foi possível carregar');expect(f.nativeElement.querySelector('visual-metric-card')).toBeNull();
 });
 it('mantém turmas sem média como nulo e situações na ordem do donut',()=>{
  const c=TestBed.createComponent(DashboardIndicadoresComponent).componentInstance;c.dados.set({alunosFizeram:2,alunosPendentes:3,mediaGeral:7.5,naoIniciadas:2,emAndamento:1,aguardandoCorrecao:1,corrigidas:2,mediasTurmas:[{turmaId:5,ensino:'F',ano:1,letra:'A',media:7.5,corrigidas:2},{turmaId:6,ensino:'M',ano:1,letra:'B',media:null,corrigidas:0}]});
  expect(c.medias).toEqual([7.5,null]);expect(c.quantidades).toEqual([2,1,1,2]);expect(c.turmas).toContain('Médio — 1º ano B');
 });
});

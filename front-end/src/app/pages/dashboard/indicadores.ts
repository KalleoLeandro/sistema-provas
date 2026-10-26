import { Component, Input, computed, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { AlertComponent, ButtonDirective, CardComponent } from '../../design-system/components';
import { MetricCardComponent } from '../../design-system/metric-card';
import { ChartComponent } from '../../design-system/extensions/chart';
import { PendenciaQuestoes } from '../../models/pendencia-questoes';
import { InfoTooltipComponent } from '../../design-system/info-tooltip';

export interface IndicadoresDashboard {
 alunosFizeram:number;alunosPendentes:number;mediaGeral:number|null;
 naoIniciadas:number;emAndamento:number;aguardandoCorrecao:number;corrigidas:number;
 mediasTurmas:{turmaId:number;ensino:'F'|'M';ano:number;letra:string;media:number|null;corrigidas:number}[];
}
@Component({selector:'app-dashboard-indicadores',imports:[DecimalPipe,AlertComponent,ButtonDirective,CardComponent,MetricCardComponent,ChartComponent,InfoTooltipComponent],templateUrl:'./indicadores.html',styleUrl:'../../design-system/dashboard-indicators.css'})
export class DashboardIndicadoresComponent implements OnInit,OnDestroy {
 @Input() showQuestions=false;
 readonly pendencias=signal<PendenciaQuestoes[]>([]);readonly pendenciasErro=signal('');
 readonly totalFaltantes=computed(()=>this.pendencias().reduce((s,p)=>s+p.quantidades.reduce((n,q)=>n+q.faltantes,0),0));
 private readonly api=inject(ApiService);private visit=0;
 readonly dados=signal<IndicadoresDashboard|null>(null);readonly loading=signal(false);readonly erro=signal('');
 get turmas(){return this.dados()?.mediasTurmas.map(t=>(t.ensino==='F'?'Fundamental':'Médio')+' — '+t.ano+'º ano '+t.letra)??[];}
 get medias(){return this.dados()?.mediasTurmas.map(t=>t.media)??[];}
 readonly situacoes=['Não iniciadas','Em andamento','Aguardando correção','Corrigidas'];
 get quantidades(){const d=this.dados();return d?[d.naoIniciadas,d.emAndamento,d.aguardandoCorrecao,d.corrigidas]:[];}
 get total(){return this.quantidades.reduce((s,v)=>s+v,0);}
 ngOnInit(){void this.load();}ngOnDestroy(){this.visit++;this.dados.set(null);}
 async load(){
  if(this.loading())return;const visit=++this.visit;this.loading.set(true);this.erro.set('');
  const results=await Promise.allSettled([this.api.get<IndicadoresDashboard>('/dashboard/indicadores'),...(this.showQuestions?[this.api.get<PendenciaQuestoes[]>('/professor/dashboard/pendencias-questoes')]:[])]);
  if(visit!==this.visit)return;
  const metric=results[0];if(metric.status==='fulfilled')this.dados.set(metric.value as IndicadoresDashboard);else{this.dados.set(null);this.erro.set('Não foi possível carregar os indicadores. Tente novamente.');}
  const pending=results[1];this.pendenciasErro.set('');if(pending?.status==='fulfilled')this.pendencias.set(pending.value as PendenciaQuestoes[]);else if(pending)this.pendenciasErro.set('Não foi possível verificar as questões pendentes. Tente novamente.');
  this.loading.set(false);
 }
}

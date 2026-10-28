import { ListSort } from '../../design-system/list-sort';
import { SortHeaderComponent } from '../../design-system/sort-header';
import { statusAluno, participaEstatisticas } from '../../models/status-aluno';
import { Component, HostListener, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { Aluno } from '../../models/aluno';
import { Turma } from '../../models/turma';
import { AlertComponent, ButtonDirective, FieldComponent, InputDirective, RecordNameDirective } from '../../design-system/components';
import { TableScrollComponent } from '../../design-system/table-scroll';
import { TabsComponent } from '../../design-system/tabs';

export interface ProvaRelatorio { id:number; titulo:string; materia:string; professor:string; turmaId:number; disponivelEm:string; encerraEm:string; }
export interface ResultadoRelatorio { alunoId:number; nome:string; numero:number|null; cgm:string; status:'M'|'T'|'R'|'D'; tentativaId:number|null; iniciadaEm:string|null; entregueEm:string|null; pendentes:number; nota:number|null; }
@Component({selector:'app-relatorios',imports: [SortHeaderComponent,FormsModule,DatePipe,AlertComponent,ButtonDirective,FieldComponent,InputDirective,RecordNameDirective,TableScrollComponent,TabsComponent],templateUrl:'./relatorios.html',styleUrl:'../../design-system/reports.css'})
export class RelatoriosComponent implements OnInit,OnDestroy {
 readonly sort=new ListSort();

 readonly api=inject(ApiService);
 readonly turmas=signal<Turma[]>([]);readonly provas=signal<ProvaRelatorio[]>([]);readonly alunos=signal<Aluno[]>([]);readonly resultados=signal<ResultadoRelatorio[]>([]);
 readonly loading=signal(false);readonly erro=signal('');readonly carregado=signal(false);
 readonly abas=[{id:'alunos',label:'Lista de alunos'},{id:'resultados',label:'Resultados das provas'}];
 formato='pdf';
 readonly camposAlunos=[{id:'numero',label:'Número'},{id:'nome',label:'Aluno'},{id:'cgm',label:'CGM'},{id:'status',label:'Matrícula'}];
 readonly camposResultados=[...this.camposAlunos,{id:'situacao',label:'Situação da prova'},{id:'entregueEm',label:'Entregue em'},{id:'nota',label:'Nota'}];
 selecionados:Record<string,boolean>={numero:true,nome:true,cgm:true,status:true,turma:false,prova:false,materia:false,professor:false,situacao:true,entregueEm:true,nota:true};
 get campos(){return this.modo==='alunos'?this.camposAlunos:this.camposResultados;}
 get colunas(){return this.campos.filter(c=>this.selecionados[c.id]);}
 get linhas():Array<Aluno|ResultadoRelatorio>{return this.sort.ordered<Aluno|ResultadoRelatorio>(this.modo==='alunos'?this.alunos():this.resultados(),(item,c)=>c==='numero'?item.numero:c==='nota'?(item as ResultadoRelatorio).nota:c==='entregueEm'?(item as ResultadoRelatorio).entregueEm:this.valor(item,c));}
 get matriculados(){return this.alunos().filter(a=>a.status==='M').length;}
 get remanejados(){return this.alunos().filter(a=>a.status==='R').length;}
 get desistentes(){return this.alunos().filter(a=>a.status==='D').length;}
 get transferidos(){return this.alunos().filter(a=>a.status==='T').length;}
 modo='alunos';turmaId:number|null=null;provaId:number|null=null;geradoEm=new Date();private visit=0;
 get turma(){return this.turmas().find(t=>t.id===this.turmaId);}
 get prova(){return this.provas().find(p=>p.id===this.provaId);}
 get totalEstatistico(){return this.linhas.filter(r=>participaEstatisticas(r.status)).length;}
 get entregues(){return this.resultados().filter(r=>participaEstatisticas(r.status)&&r.entregueEm);}
 get corrigidas(){return this.entregues.filter(r=>!r.pendentes);}
 get media(){const itens=this.corrigidas;return itens.length?itens.reduce((s,r)=>s+(r.nota??0),0)/itens.length:null;}
 ngOnInit(){void this.init();}ngOnDestroy(){this.clear();}
 @HostListener('window:pagehide') clear(){this.visit++;this.alunos.set([]);this.resultados.set([]);this.carregado.set(false);this.loading.set(false);}
 @HostListener('window:pageshow',['$event']) restore(event:PageTransitionEvent){if(event.persisted)void this.changeClass();}
 async init(){const visit=++this.visit;this.loading.set(true);try{const turmas=await this.api.get<Turma[]>('/relatorios/turmas');if(visit===this.visit)this.turmas.set(turmas);}catch{if(visit===this.visit)this.erro.set('Não foi possível carregar as turmas. Tente novamente.');}finally{if(visit===this.visit)this.loading.set(false);}}
 changeMode(modo:string){this.modo=modo;void this.changeClass();}
 async changeClass(){
  const visit=++this.visit;this.provaId=null;this.provas.set([]);this.alunos.set([]);this.resultados.set([]);this.erro.set('');this.carregado.set(false);this.loading.set(false);if(!this.turmaId)return;
  this.loading.set(true);
  try{if(this.modo==='alunos'){const alunos=await this.api.get<Aluno[]>('/relatorios/alunos?turmaId='+this.turmaId);if(visit!==this.visit)return;this.alunos.set(alunos);this.carregado.set(true);this.geradoEm=new Date();}
   else{const provas=await this.api.get<ProvaRelatorio[]>('/relatorios/provas?turmaId='+this.turmaId);if(visit===this.visit)this.provas.set(provas);}}
  catch{if(visit===this.visit)this.erro.set('Não foi possível gerar o relatório. Tente novamente.');}finally{if(visit===this.visit)this.loading.set(false);}
 }
 async loadResults(){const visit=++this.visit;this.resultados.set([]);this.carregado.set(false);this.erro.set('');this.loading.set(false);if(!this.provaId)return;this.loading.set(true);
  try{const itens=await this.api.get<ResultadoRelatorio[]>('/relatorios/resultados?provaId='+this.provaId);if(visit!==this.visit)return;this.resultados.set(itens);this.geradoEm=new Date();this.carregado.set(true);}
  catch{if(visit===this.visit)this.erro.set('Não foi possível gerar os resultados. Tente novamente.');}finally{if(visit===this.visit)this.loading.set(false);}
 }
 situacao(r:ResultadoRelatorio){return !r.tentativaId?'Não iniciada':!r.entregueEm?'Em andamento':r.pendentes?'Aguardando correção':'Corrigida';}
 /** Formata os mesmos campos para visualização, impressão e CSV. */
 valor(item:Aluno|ResultadoRelatorio,campo:string):string {
  const r=item as ResultadoRelatorio;
  switch(campo){
   case 'numero':return item.numero===null?'—':String(item.numero);
   case 'nome':return item.nome;case 'cgm':return item.cgm;
   case 'status':return statusAluno(item.status);
   case 'turma':return this.turma?(this.turma.ensino==='F'?'Fundamental':'Médio')+' — '+this.turma.ano+'º ano '+this.turma.letra:'';
   case 'prova':return this.prova?.titulo??'';case 'materia':return this.prova?.materia??'';case 'professor':return this.prova?.professor??'';
   case 'situacao':return this.situacao(r);
   case 'entregueEm':return r.entregueEm?new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date(r.entregueEm)):'—';
   case 'nota':return r.nota===null?'—':r.nota.toLocaleString('pt-BR',{minimumFractionDigits:2,maximumFractionDigits:2})+(r.entregueEm&&r.pendentes?' (parcial)':'');
   default:return '';
  }
 }
 /** Exporta todas as linhas da consulta com os campos escolhidos, sem paginação. */
 csv(){
  const quote=(valor:string)=>'"'+(/^[\s]*[=+@-]/.test(valor)?"'"+valor:valor).replaceAll('"','""')+'"';
  return '\ufeff'+[this.colunas.map(c=>c.label),...this.linhas.map(r=>this.colunas.map(c=>this.valor(r,c.id)))].map(row=>row.map(quote).join(';')).join('\r\n');
 }
 export(){
  if(!this.carregado()||this.loading()||this.erro()||!this.colunas.length)return;
  if(this.formato==='pdf'){window.print();return;}
  const url=URL.createObjectURL(new Blob([this.csv()],{type:'text/csv;charset=utf-8;'}));
  const link=document.createElement('a');link.href=url;link.download='esfinora-'+this.modo+'-turma-'+this.turmaId+'.csv';link.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
 }

}

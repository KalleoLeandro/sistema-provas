import { Component,inject,OnInit,OnDestroy,signal,HostListener } from '@angular/core';
import { DatePipe,DecimalPipe } from '@angular/common';
import { FormsModule,NgForm } from '@angular/forms';
import { ActivatedRoute,RouterLink } from '@angular/router';
import { AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective } from '../../../design-system/components';
import { InfoTooltipComponent } from '../../../design-system/info-tooltip';
import { ValidationMessagePipe } from '../../../design-system/validators';
import { ApiService } from '../../../services/api.service';
import { ProvaCorrecao,QuestaoCorrecao,RascunhoCorrecao } from '../../../models/correcao';
@Component({selector:'app-corrigir-prova',imports:[FormsModule,RouterLink,DatePipe,DecimalPipe,AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,InfoTooltipComponent,ValidationMessagePipe],templateUrl:'./corrigir.html',styleUrl:'../../../design-system/exam-correction.css'})
export class CorrigirProvaComponent implements OnInit,OnDestroy {
 private readonly api=inject(ApiService);private readonly route=inject(ActivatedRoute);private readonly id=Number(this.route.snapshot.paramMap.get('id'));
 readonly dados=signal<ProvaCorrecao|null>(null);readonly loading=signal(false);readonly saving=signal(false);readonly erro=signal('');readonly saved=signal(false);
 drafts:Record<number,RascunhoCorrecao>={};private visit=0;
 ngOnInit(){void this.load();}ngOnDestroy(){this.visit++;this.drafts={};}
 @HostListener('window:pagehide') clear(){this.visit++;this.drafts={};this.dados.set(null);this.saving.set(false);this.loading.set(false);}
 @HostListener('window:pageshow',['$event']) restore(event:PageTransitionEvent){if(event.persisted)void this.load();}
 /** Carrega somente a cópia entregue e inicia os rascunhos de correção em memória. */
 async load(){if(this.saving())return;const visit=++this.visit;this.loading.set(true);this.erro.set('');this.saved.set(false);try{const p=await this.api.get<ProvaCorrecao>('/professor/correcoes/'+this.id);if(visit!==this.visit)return;this.dados.set(p);this.drafts={};for(const q of p.questoes)this.drafts[q.numero]={pontuacao:q.pontuacao,feedback:q.feedback??'',revisao:q.revisao};}catch(e){if(visit===this.visit){this.dados.set(null);this.erro.set(this.message(e));}}finally{if(visit===this.visit)this.loading.set(false);}}
 /** Valida todos os campos e grava a prova inteira em uma única operação. */
 async save(form:NgForm){
  if(this.saving())return;form.control.markAllAsTouched();if(form.invalid)return;const dados=this.dados();if(!dados)return;
  for(const q of dados.questoes){const draft=this.drafts[q.numero];if(!draft||draft.pontuacao===null||draft.pontuacao<0||draft.pontuacao>q.peso){this.erro.set('Revise a pontuação da questão '+q.numero+'.');return;}}
  const questoes=dados.questoes.map(q=>({numero:q.numero,correcao:{...this.drafts[q.numero]}}));
  const visit=this.visit;this.saving.set(true);this.erro.set('');this.saved.set(false);
  try{const result=await this.api.put<ProvaCorrecao>('/professor/correcoes/'+this.id,{questoes});if(visit!==this.visit)return;this.dados.set(result);for(const q of result.questoes)this.drafts[q.numero]={pontuacao:q.pontuacao,feedback:q.feedback??'',revisao:q.revisao};this.saved.set(true);}
  catch(e){if(visit===this.visit)this.erro.set(this.message(e));}finally{if(visit===this.visit)this.saving.set(false);}
 }
 canGrade(q:QuestaoCorrecao){return q.tipo==='discursiva'&&!!q.resposta?.trim();}
 private message(e:unknown){const body=(e as {error?:{mensagem?:string;erros?:{campo:string;mensagem:string}[]}})?.error;return body?.erros?.length?body.erros.map(v=>v.campo+': '+v.mensagem).join(' '):body?.mensagem??'Não foi possível carregar ou salvar a correção. Tente novamente.';}
}

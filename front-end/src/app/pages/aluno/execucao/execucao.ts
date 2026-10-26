import { Component,HostListener,inject,OnDestroy,OnInit,signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute,Router,RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective } from '../../../design-system/components';
import { ConfirmDialogComponent } from '../../../design-system/confirm-dialog';
import { QuestionNavigationComponent } from '../../../design-system/question-navigation';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { RegrasProva,TentativaProva } from '../../../models/execucao-prova';
@Component({selector:'app-execucao-prova',imports:[DatePipe,FormsModule,RouterLink,AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,ConfirmDialogComponent,QuestionNavigationComponent],templateUrl:'./execucao.html',styleUrl:'../../../design-system/exam-execution.css'})
export class ExecucaoProvaComponent implements OnInit,OnDestroy {
 private readonly api=inject(ApiService);private readonly auth=inject(AuthService);private readonly router=inject(Router);private readonly route=inject(ActivatedRoute);
 readonly rules=signal<RegrasProva|null>(null);readonly attempt=signal<TentativaProva|null>(null);readonly loading=signal(true);readonly error=signal('');
 readonly deliveryOpen=signal(false);readonly acknowledging=signal(false);
 readonly saving=signal(false);readonly starting=signal(false);readonly finishing=signal(false);readonly remaining=signal(0);readonly confirmOpen=signal(false);readonly pendingCount=signal(0);
 token='';accepted=false;answers:Record<number,string>={};
 private readonly id=Number(this.route.snapshot.paramMap.get('id'));
 private pending=new Map<number,string>();private timers=new Map<number,ReturnType<typeof setTimeout>>();private revisions=new Map<number,number>();
 private task:Promise<void>|null=null;private ticker?:ReturnType<typeof setInterval>;private syncer?:ReturnType<typeof setInterval>;private destroyed=false;
 private serverTime=0;private measuredAt=0;private retryAfter=0;
 get readonly(){return this.attempt()?.situacao==='submetida';}
 get answered(){return Object.entries(this.answers).filter(([,value])=>!!value.trim()).map(([number])=>Number(number));}
 get missing(){return (this.attempt()?.questoes.length??10)-this.answered.length;}
 get clock(){const seconds=Math.ceil(this.remaining()/1000);return Math.floor(seconds/3600).toString().padStart(2,'0')+':'+Math.floor(seconds%3600/60).toString().padStart(2,'0')+':'+(seconds%60).toString().padStart(2,'0');}
 ngOnInit(){void this.load();this.ticker=setInterval(()=>this.tick(),500);this.syncer=setInterval(()=>{if(this.attempt()&&!this.readonly&&!this.saving()&&!this.pendingCount()&&!this.finishing())void this.sync();},15000);}
 ngOnDestroy(){this.destroyed=true;clearInterval(this.ticker);clearInterval(this.syncer);for(const timer of this.timers.values())clearTimeout(timer);this.token='';}
 /** Carrega somente regras até existir uma tentativa confirmada. */
 async load(){this.loading.set(true);this.error.set('');try{const rules=await this.api.get<RegrasProva>('/aluno/provas/'+this.id+'/regras');if(this.destroyed)return;this.rules.set(rules);if(rules.iniciada){const attempt=await this.api.get<TentativaProva>('/aluno/provas/'+this.id+'/tentativa');await this.apply(attempt,true);}}catch(e){this.error.set(this.message(e));}finally{this.loading.set(false);}}
 /** O sorteio é feito exclusivamente pelo servidor após o aceite e token. */
 async start(){if(this.starting()||!this.accepted||this.token.length<4)return;this.starting.set(true);this.error.set('');try{await this.apply(await this.api.post<TentativaProva>('/aluno/provas/'+this.id+'/iniciar',{token:this.token,aceitouRegras:this.accepted}),true);}catch(e){this.error.set(this.message(e));}finally{this.token='';this.starting.set(false);}}
 /** Mantém rascunho em memória somente até a confirmação do salvamento no servidor. */
 change(number:number,immediate=false){if(this.readonly||this.finishing()||this.remaining()<=0)return;const timer=this.timers.get(number);if(timer)clearTimeout(timer);if(immediate){this.pending.set(number,this.answers[number]);this.updatePending();void this.pump();}else{this.timers.set(number,setTimeout(()=>{this.timers.delete(number);this.pending.set(number,this.answers[number]);this.updatePending();void this.pump();},400));this.updatePending();}}
 /** Serializa pedidos para que a última marcação use a revisão confirmada pelo servidor. */
 private pump():Promise<void>{if(this.task)return this.task;this.task=this.saveLoop().finally(()=>{this.task=null;this.saving.set(false);this.updatePending();});return this.task;}
 private async saveLoop(){this.saving.set(true);while(this.pending.size&&!this.readonly){const [number,value]=this.pending.entries().next().value!;this.pending.delete(number);try{const result=await this.api.put<TentativaProva>('/aluno/provas/'+this.id+'/questoes/'+number+'/resposta',{resposta:value,revisao:this.revisions.get(number)??0});await this.apply(result,false);if(result.situacao==='submetida')return;this.error.set('');}catch(e){if(!this.pending.has(number))this.pending.set(number,this.answers[number]);this.error.set(this.message(e));break;}}}
 /** Aguarda alterações pendentes antes de permitir navegação ou entrega. */
 async flush(){for(const [number,timer]of this.timers){clearTimeout(timer);this.pending.set(number,this.answers[number]);}this.timers.clear();await this.pump();return !this.pending.size;}
 async tryLeave(){return this.readonly||!this.attempt()||await this.flush();}
 @HostListener('window:beforeunload',['$event']) beforeUnload(event:BeforeUnloadEvent){if(this.pendingCount()||this.saving()){event.preventDefault();event.returnValue='';}}
 /** Confirma toda entrega manual e destaca as questões ainda em branco. */
 async requestSubmit(){if(this.finishing()||this.readonly)return;if(!await this.flush())return;this.confirmOpen.set(true);}
 /** Entrega no servidor e encerra a sessão; expiradas enviam apenas o que já estava salvo. */
 async submit(confirmIncomplete:boolean,expired=false){if(this.finishing()||this.readonly)return;this.finishing.set(true);this.confirmOpen.set(false);this.error.set('');try{if(!expired&&!await this.flush())return;const result=await this.api.post<TentativaProva>('/aluno/provas/'+this.id+'/submeter',{confirmarIncompleta:confirmIncomplete});await this.apply(result,false);}catch(e){this.retryAfter=performance.now()+3000;this.error.set(this.message(e));}finally{this.finishing.set(false);}}
 retrySave(){void this.flush();}
 focus(number:number){document.getElementById('questao-'+number)?.scrollIntoView({behavior:'smooth',block:'start'});document.getElementById('questao-'+number)?.focus({preventScroll:true});}
 private updatePending(){this.pendingCount.set(this.pending.size+this.timers.size);}
 private async apply(value:TentativaProva,initial:boolean){if(this.destroyed)return;const wasActive=this.attempt()?.situacao==='em_andamento';this.attempt.set(value);this.serverTime=Date.parse(value.agora);this.measuredAt=performance.now();for(const q of value.questoes){this.revisions.set(q.numero,Math.max(q.revisao,this.revisions.get(q.numero)??0));if(initial)this.answers[q.numero]=q.resposta??'';}if(value.enviadaAgora||(!initial&&wasActive&&value.situacao==='submetida')){this.showDelivery();return;}this.tick();}
 private tick(){const a=this.attempt();if(!a||this.readonly)return;this.remaining.set(Math.max(0,Date.parse(a.encerraEm)-(this.serverTime+performance.now()-this.measuredAt)));if(!this.remaining()&&!this.finishing()&&performance.now()>=this.retryAfter){for(const t of this.timers.values())clearTimeout(t);this.timers.clear();this.pending.clear();this.updatePending();void this.submit(true,true);}}
 private async sync(){try{await this.apply(await this.api.get<TentativaProva>('/aluno/provas/'+this.id+'/tentativa'),false);}catch(e){this.error.set(this.message(e));}}
 /** Exibe o recibo somente após confirmação da entrega pelo servidor. */
 private showDelivery(){this.pending.clear();for(const timer of this.timers.values())clearTimeout(timer);this.timers.clear();this.updatePending();this.confirmOpen.set(false);this.deliveryOpen.set(true);}
 /** OK, fechar e Escape encerram o aviso e seguem para o login. */
 async acknowledgeDelivery(){if(this.acknowledging())return;this.acknowledging.set(true);try{await this.auth.logout();await this.router.navigateByUrl('/login');this.deliveryOpen.set(false);}catch{this.error.set('A prova foi entregue. Não foi possível sair agora; clique em OK para tentar novamente.');}finally{this.acknowledging.set(false);}}
 private message(error:unknown){return (error as {error?:{mensagem?:string}})?.error?.mensagem??'Não foi possível salvar ou carregar a prova. Verifique sua conexão e tente novamente.';}
}

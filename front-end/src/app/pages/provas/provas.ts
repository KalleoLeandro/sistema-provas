import { AssuntoProva } from '../../models/assunto';
import { ChipSelectComponent } from '../../design-system/chip-select';
import { ListSort } from '../../design-system/list-sort';
import { SortHeaderComponent } from '../../design-system/sort-header';
import { statusAluno } from '../../models/status-aluno';
import { Component, HostListener, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ListarCorrecoesComponent } from './correcoes/listar';
import { DateFieldComponent } from '../../design-system/date-field';
import { TabsComponent } from '../../design-system/tabs';
import { DatePipe } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective, RecordNameDirective } from '../../design-system/components';
import { TableScrollComponent } from '../../design-system/table-scroll';
import { PaginationComponent } from '../../design-system/pagination';
import { InfoTooltipComponent } from '../../design-system/info-tooltip';
import { NotBlankDirective, ValidationMessagePipe } from '../../design-system/validators';
import { ApiService } from '../../services/api.service';
import { Aluno, Pagina } from '../../models/aluno';
import { Professor } from '../../models/professor';
import { Turma } from '../../models/turma';
import { LiberacaoProva,LiberacaoDetalhe } from '../../models/prova';

@Component({ selector:'app-provas', imports:[ChipSelectComponent,SortHeaderComponent,ListarCorrecoesComponent,DateFieldComponent,TabsComponent,FormsModule,DatePipe,AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,RecordNameDirective,TableScrollComponent,PaginationComponent,InfoTooltipComponent,NotBlankDirective,ValidationMessagePipe],
  templateUrl:'./provas.html',styleUrls:['../../design-system/list-filters.css','../../design-system/exam-releases.css'] })
export class ProvasComponent implements OnInit,OnDestroy {
 readonly sort=new ListSort();
 readonly studentSort=new ListSort();
 changeSort(){this.paginaProvas=1;void this.loadProvas();}
 changeStudentSort(){this.pagina=1;void this.load();}

 readonly statusAluno=statusAluno;
  private readonly api=inject(ApiService);
  private readonly route=inject(ActivatedRoute,{optional:true});
  assuntos:AssuntoProva[]=[];assuntoIds:number[]=[];readonly loadingAssuntos=signal(false);private assuntoVisit=0;
  get opcoesAssuntos(){return this.assuntos.map(a=>({value:a.id,label:a.nome}));}
  get assuntosEscolhidos(){return this.assuntos.filter(a=>this.assuntoIds.includes(a.id));}
  get assuntosValidos(){return this.assuntoIds.length>0&&this.assuntoIds.length<=10&&this.assuntoIds.every(id=>this.assuntos.some(a=>a.id===id))&&this.assuntosEscolhidos.some(a=>a.completo);}
  assuntosLabel(p:LiberacaoProva){return p.assuntos?.map(a=>a.nome).join(', ')??'';}
  changeMateriaProva(){this.assuntoIds=[];void this.loadAssuntos();}
  async loadAssuntos(){const turma=this.turmaLiberacaoId,materia=this.materiaId,visit=++this.assuntoVisit;this.assuntos=[];this.loadingAssuntos.set(true);
    try{if(turma&&materia){const rows=await this.api.get<AssuntoProva[]>('/professor/provas/turmas/'+turma+'/assuntos?materiaId='+materia);if(visit===this.assuntoVisit)this.assuntos=rows;}}
    catch{if(visit===this.assuntoVisit)this.erroProvas.set('Não foi possível carregar os assuntos e suas quantidades.');}finally{if(visit===this.assuntoVisit)this.loadingAssuntos.set(false);}}
  readonly alunos=signal<Aluno[]>([]);
  readonly turmas=signal<Turma[]>([]);
  readonly loading=signal(false);
  readonly erro=signal('');
  readonly selecionado=signal<Aluno|null>(null);
  readonly provas=signal<LiberacaoProva[]>([]);
  readonly loadingProvas=signal(false);
  readonly busy=signal(false);
  readonly erroProvas=signal('');
  readonly mensagem=signal('');
  readonly formOpen=signal(false);
  readonly editingBusy=signal(false);
  tokenIndisponivel=false;
  private editVisit=0;
  private materias: Professor['materias']=[];
  modo=this.route?.snapshot.queryParamMap.get('aba')==='correcoes'?'correcoes':'turmas';
  readonly abas=[{id:'turmas',label:'Liberações por turma'},{id:'alunos',label:'Consulta por aluno'},{id:'correcoes',label:'Correções'}];
  turmaId:number|null=null;
  turmaLiberacaoId:number|null=null;
  pagina=1;tamanhoPagina=15;total=0;
  paginaProvas=1;tamanhoProvas=15;totalProvas=0;
  duracaoMinutos=60;token='';editingId:number|null=null;
  titulo='';materiaId:number|null=null;disponivelEm='';encerraEm='';
  private visit=0;private proofVisit=0;
  private timer?:ReturnType<typeof setInterval>;
  ngOnInit(){void this.init();this.timer=setInterval(()=>{if((this.modo==='turmas'?this.turmaLiberacaoId:this.selecionado())&&!this.busy()&&!this.loadingProvas())void this.loadProvas();},30000);}
  ngOnDestroy(){this.visit++;this.close();clearInterval(this.timer);}
  @HostListener('window:pagehide') clearPage(){this.close();}
  get materiasTurma(){return this.materias.filter(m=>m.turmas.some(t=>t.id===this.turmaLiberacaoId));}
  get periodoValido(){const inicio=Date.parse(this.disponivelEm),fim=Date.parse(this.encerraEm);return Number.isFinite(inicio)&&Number.isFinite(fim)&&fim>inicio&&fim>Date.now();}

  /** Carrega somente as turmas e matérias da identidade autenticada. */
  async init(){
    this.loading.set(true);this.erro.set('');
    try{const p=await this.api.get<Professor>('/professor/dashboard');this.materias=p.materias;
      const turmas=new Map<number,Turma>();p.materias.forEach(m=>m.turmas.forEach(t=>turmas.set(t.id,t)));this.turmas.set([...turmas.values()]);await this.load();
    }catch{this.loading.set(false);this.erro.set('Não foi possível carregar os alunos. Tente novamente.');}
  }
  /** Pesquisa alunos com paginação, descartando respostas de filtros anteriores. */
  async load(){
    if(this.busy())return;this.close();const visit=++this.visit;this.loading.set(true);this.erro.set('');this.alunos.set([]);
    try{const r=await this.api.get<Pagina<Aluno>>('/professor/alunos?pagina='+this.pagina+'&tamanho='+this.tamanhoPagina+(this.turmaId?'&turmaId='+this.turmaId:'')+this.studentSort.query());
      if(visit!==this.visit)return;this.alunos.set(r.itens);this.total=r.total;this.pagina=r.pagina;
    }catch{if(visit===this.visit){this.total=0;this.erro.set('Não foi possível carregar os alunos. Tente novamente.');}}
    finally{if(visit===this.visit)this.loading.set(false);}
  }
  /** Abre a relação de liberações do aluno ou recolhe a mesma linha. */
  async toggle(aluno:Aluno){
    if(this.busy())return;if(this.selecionado()?.id===aluno.id){this.close();return;}
    this.close();this.selecionado.set(aluno);
    await this.loadProvas();
  }
  /** Consulta apenas liberações próprias; a atualização periódica não apaga o formulário. */
  async loadProvas(){
    const id=this.modo==='turmas'?this.turmaLiberacaoId:this.selecionado()?.id;if(!id)return;const visit=++this.proofVisit;this.loadingProvas.set(true);this.erroProvas.set('');
    try{const r=await this.api.get<Pagina<LiberacaoProva>>('/professor/provas/'+(this.modo==='turmas'?'turmas/':'alunos/')+id+'?pagina='+this.paginaProvas+'&tamanho='+this.tamanhoProvas+this.sort.query());
      if(visit!==this.proofVisit)return;this.provas.set(r.itens);this.totalProvas=r.total;this.paginaProvas=r.pagina;
    }catch(error){if(visit===this.proofVisit){this.provas.set([]);this.totalProvas=0;this.erroProvas.set(this.errorMessage(error));}}
    finally{if(visit===this.proofVisit)this.loadingProvas.set(false);}
  }
  /** Grava o prazo em UTC, preservando o preenchimento quando a API recusa a operação. */
  async release(form:NgForm){
    const turmaId=this.turmaLiberacaoId;if(this.modo!=='turmas'||!turmaId||this.busy()||!this.formOpen())return;form.control.markAllAsTouched();this.mensagem.set('');
    if(form.invalid||!this.assuntosValidos||this.loadingAssuntos()||!this.periodoValido||!this.materiasTurma.some(m=>m.materiaId===this.materiaId))return;
    this.busy.set(true);this.erroProvas.set('');
    try{const editing=this.editingId;const body={titulo:this.titulo.trim(),materiaId:this.materiaId,assuntos:[...this.assuntoIds],disponivelEm:new Date(this.disponivelEm).toISOString(),encerraEm:new Date(this.encerraEm).toISOString(),duracaoMinutos:this.duracaoMinutos,token:this.token||null};
      const path='/professor/provas/turmas/'+turmaId+'/liberacoes';
      if(editing)await this.api.put(path+'/'+editing,body);else await this.api.post(path,body);
      this.reset(form);this.cancelEditor();this.mensagem.set(editing?'Liberação atualizada com sucesso.':'Liberação registrada com sucesso.');this.paginaProvas=1;await this.loadProvas();
    }catch(error){this.erroProvas.set(this.errorMessage(error));}
    finally{this.busy.set(false);}
  }
  /** Limpa os dados temporários sem alterar liberações já registradas. */
  reset(form:NgForm){this.assuntoIds=[];this.tokenIndisponivel=false;this.token='';this.duracaoMinutos=60;this.editingId=null;this.titulo='';this.encerraEm='';this.disponivelEm=this.localNow();this.materiaId=this.materiasTurma.length===1?this.materiasTurma[0].materiaId:null;
    void this.loadAssuntos();form.resetForm({assuntos:[],titulo:'',materiaId:this.materiaId,disponivelEm:this.disponivelEm,encerraEm:'',duracaoMinutos:60,token:''});this.mensagem.set('');}
  /** Descarta a seleção e invalida consultas em andamento ao fechar ou sair. */
  close(){this.cancelEditor();this.proofVisit++;this.selecionado.set(null);this.provas.set([]);this.erroProvas.set('');this.mensagem.set('');this.loadingProvas.set(false);
    this.token='';this.duracaoMinutos=60;this.editingId=null;this.titulo='';this.materiaId=null;this.disponivelEm='';this.encerraEm='';this.paginaProvas=1;this.totalProvas=0;}
  /** Troca a turma e limpa o formulário para evitar liberar para a turma anterior. */
  async changeReleaseClass(){this.close();await this.loadProvas();}
  /** Alterna liberação coletiva e consulta individual sem manter rascunhos. */
  changeMode(mode:string){if(this.busy())return;this.close();this.turmaLiberacaoId=null;this.modo=mode;if(mode==='alunos')void this.load();}
  /** Abre um cadastro vazio apenas por ação explícita do professor. */
  newRelease(){if(this.busy()||this.editingBusy()||!this.turmaLiberacaoId)return;this.cancelEditor();this.disponivelEm=new Date().toISOString();this.materiaId=this.materiasTurma.length===1?this.materiasTurma[0].materiaId:null;this.mensagem.set('');void this.loadAssuntos();this.formOpen.set(true);}
  /** Fecha e descarta campos temporários, preservando a lista consultada. */
  cancelEditor(){this.assuntoVisit++;this.assuntos=[];this.assuntoIds=[];this.loadingAssuntos.set(false);this.editVisit++;this.formOpen.set(false);this.editingBusy.set(false);this.editingId=null;this.token='';this.tokenIndisponivel=false;this.titulo='';this.materiaId=null;this.disponivelEm='';this.encerraEm='';this.duracaoMinutos=60;}
  /** Reconsulta a liberação pelo ID para preencher dados atuais, inclusive o token consultável. */
  async edit(prova:LiberacaoProva){
    if(this.busy()||this.editingBusy()||!this.turmaLiberacaoId)return;this.cancelEditor();const visit=++this.editVisit;this.editingBusy.set(true);this.erroProvas.set('');
    try{const p=await this.api.get<LiberacaoDetalhe>('/professor/provas/turmas/'+this.turmaLiberacaoId+'/liberacoes/'+prova.id);if(visit!==this.editVisit)return;
      this.editingId=p.id;this.titulo=p.titulo;this.materiaId=p.materiaId;await this.loadAssuntos();if(visit!==this.editVisit)return;this.assuntoIds=(p.assuntos??[]).map(a=>a.id);this.duracaoMinutos=p.duracaoMinutos;this.token=p.token??'';this.tokenIndisponivel=p.precisaNovoToken;
      this.disponivelEm=p.disponivelEm;this.encerraEm=p.encerraEm;this.mensagem.set('');this.formOpen.set(true);
    }catch(error){if(visit===this.editVisit)this.erroProvas.set(this.errorMessage(error));}
    finally{if(visit===this.editVisit)this.editingBusy.set(false);}
  }
  changeTurma(){this.pagina=1;void this.load();}
  changeSize(size:number){this.tamanhoPagina=size;this.pagina=1;void this.load();}
  changePage(page:number){this.pagina=page;void this.load();}
  changeProofPage(page:number){this.paginaProvas=page;void this.loadProvas();}
  changeProofSize(size:number){this.tamanhoProvas=size;this.paginaProvas=1;void this.loadProvas();}
  private localNow(){const now=new Date();return new Date(now.getTime()-now.getTimezoneOffset()*60000).toISOString().slice(0,16);}
  private errorMessage(error:unknown){const body=(error as {error?:{mensagem?:string;erros?:{campo:string;mensagem:string}[]}})?.error;
    return body?.erros?.length?body.erros.map(e=>e.campo+': '+e.mensagem).join(' '):body?.mensagem??'Não foi possível carregar ou registrar a liberação. Tente novamente.';}
}

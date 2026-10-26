import {Component,HostListener,OnInit,OnDestroy,inject,signal,computed} from '@angular/core';
import {FormsModule,NgForm} from '@angular/forms';
import {ActivatedRoute,Router,RouterLink} from '@angular/router';
import {ApiService} from '../../../services/api.service';
import {Aluno} from '../../../models/aluno';
import {Professor} from '../../../models/professor';
import {Turma} from '../../../models/turma';
import {Materia} from '../../../models/materia';
import {AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective} from '../../../design-system/components';
import {ChipSelectComponent} from '../../../design-system/chip-select';
import {AllowedValuesDirective,NotBlankDirective,TrimmedMinlengthDirective,ValidationMessagePipe} from '../../../design-system/validators';

@Component({selector:'app-editar-cadastro',imports:[FormsModule,RouterLink,AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,ChipSelectComponent,AllowedValuesDirective,NotBlankDirective,TrimmedMinlengthDirective,ValidationMessagePipe],templateUrl:'./editar-cadastro.html'})
export class EditarCadastroComponent implements OnInit,OnDestroy {
 private readonly api=inject(ApiService);private readonly router=inject(Router);private readonly route=inject(ActivatedRoute);
 readonly perfil=this.route.snapshot.data['perfil'] as 'professor'|'aluno';private readonly id=Number(this.route.snapshot.paramMap.get('id'));private visit=0;
 readonly loading=signal(false);readonly loaded=signal(false);readonly busy=signal(false);readonly erro=signal('');
 readonly turmas=signal<Turma[]>([]);readonly catalogoMaterias=signal<Materia[]>([]);
 readonly opcoesMaterias=computed(()=>this.catalogoMaterias().map(m=>({value:m.id,label:m.nome})));
 readonly opcoesTurmas=computed(()=>this.turmas().map(t=>({value:t.id,label:(t.ensino==='F'?'Fundamental':'Médio')+' — '+t.ano+'º ano '+t.letra})));
 nome='';login='';senha='';numero:number|null=null;cgm='';status='M';turmaId:number|null=null;ativo=true;
 materias:number[]=[];turmasPorMateria:Record<number,number[]>={};
 ngOnInit(){void this.load();}ngOnDestroy(){this.clear();}
 @HostListener('window:pagehide') clear(){this.visit++;this.nome='';this.login='';this.senha='';this.cgm='';this.numero=null;this.status='M';this.turmaId=null;this.materias=[];this.turmasPorMateria={};this.loaded.set(false);this.busy.set(false);}
 @HostListener('window:pageshow',['$event']) restore(event:PageTransitionEvent){if(event.persisted)void this.load();}
 async load(){
  if(this.busy())return;const visit=++this.visit;this.loading.set(true);this.loaded.set(false);this.erro.set('');
  try{
   const [cadastro,turmas,materias]=await Promise.all([this.api.get<Aluno|Professor>((this.perfil==='professor'?'/professores/':'/alunos/')+this.id),this.api.get<Turma[]>('/turmas'),this.perfil==='professor'?this.api.get<Materia[]>('/materias'):Promise.resolve([])]);
   if(visit!==this.visit)return;this.nome=cadastro.nome;this.senha='';this.turmas.set(turmas);this.catalogoMaterias.set(materias);
   if(this.perfil==='professor'){const p=cadastro as Professor;this.login=p.login;this.ativo=p.ativo;this.materias=p.materias.map(m=>m.materiaId);this.turmasPorMateria=Object.fromEntries(p.materias.map(m=>[m.materiaId,m.turmas.map(t=>t.id)]));}
   else{const a=cadastro as Aluno;this.numero=a.numero;this.cgm=a.cgm;this.status=a.status;this.turmaId=a.turmaId;}
   this.loaded.set(true);
  }catch(e){if(visit===this.visit)this.erro.set(this.message(e));}finally{if(visit===this.visit)this.loading.set(false);}
 }
 changeMaterias(ids:number[]){this.materias=[...ids];this.turmasPorMateria=Object.fromEntries(ids.map(id=>[id,this.turmasPorMateria[id]??[]]));}
 materiaNome(id:number){return this.catalogoMaterias().find(m=>m.id===id)?.nome??'Matéria';}
 async save(form:NgForm){
  if(this.busy()||!this.loaded())return;form.control.markAllAsTouched();if(form.invalid)return;
  if(this.perfil==='professor'&&(!this.materias.length||this.login.trim().length<3))return;
  const visit=this.visit;this.busy.set(true);this.erro.set('');
  const body=this.perfil==='professor'?{nome:this.nome.trim(),login:this.login.trim(),senha:this.senha||null,materias:this.materias.map(materiaId=>({materiaId,turmas:this.turmasPorMateria[materiaId]??[]}))}:{nome:this.nome.trim(),numero:this.numero,cgm:this.cgm.trim(),status:this.status,turmaId:this.turmaId};
  try{await this.api.put((this.perfil==='professor'?'/professores/':'/alunos/')+this.id,body);if(visit===this.visit)await this.router.navigate(['/cadastro/usuarios/listar'],{queryParams:{perfil:this.perfil}});}
  catch(e){if(visit===this.visit)this.erro.set(this.message(e));}finally{if(visit===this.visit){this.senha='';this.busy.set(false);}}
 }
 private message(e:unknown){const b=(e as {error?:{mensagem?:string;erros?:{campo:string;mensagem:string}[]}})?.error;return b?.erros?.length?b.erros.map(v=>v.campo+': '+v.mensagem).join(' '):b?.mensagem??'Não foi possível carregar ou salvar o cadastro. Tente novamente.';}
}

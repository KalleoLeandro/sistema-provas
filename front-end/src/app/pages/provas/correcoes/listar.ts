import { ListSort } from '../../../design-system/list-sort';
import { SortHeaderComponent } from '../../../design-system/sort-header';
import { Component,inject,OnInit,OnDestroy,signal } from '@angular/core';
import { DatePipe,DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,RecordNameDirective } from '../../../design-system/components';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { PaginationComponent } from '../../../design-system/pagination';
import { ApiService } from '../../../services/api.service';
import { Pagina } from '../../../models/aluno';
import { Professor } from '../../../models/professor';
import { Turma } from '../../../models/turma';
import { ProvaCorrecaoResumo } from '../../../models/correcao';
@Component({selector:'app-correcoes',imports:[SortHeaderComponent,FormsModule,DatePipe,DecimalPipe,RouterLink,AlertComponent,ButtonDirective,CardComponent,FieldComponent,InputDirective,RecordNameDirective,TableScrollComponent,PaginationComponent],templateUrl:'./listar.html',styleUrl:'../../../design-system/list-filters.css'})
export class ListarCorrecoesComponent implements OnInit,OnDestroy {
 readonly sort=new ListSort();
 changeSort(){this.filter();}

 private readonly api=inject(ApiService);readonly provas=signal<ProvaCorrecaoResumo[]>([]);readonly turmas=signal<Turma[]>([]);readonly loading=signal(false);readonly erro=signal('');
 turmaId:number|null=null;situacao='todas';pagina=1;tamanho=15;total=0;private visit=0;
 ngOnInit(){void this.init();}ngOnDestroy(){this.visit++;}
 async init(){try{const p=await this.api.get<Professor>('/professor/dashboard');const turmas=new Map<number,Turma>();p.materias.forEach(m=>m.turmas.forEach(t=>turmas.set(t.id,t)));this.turmas.set([...turmas.values()]);await this.load();}catch{this.erro.set('Não foi possível carregar as provas para correção.');}}
 async load(){const visit=++this.visit;this.loading.set(true);this.erro.set('');try{const p=await this.api.get<Pagina<ProvaCorrecaoResumo>>('/professor/correcoes?pagina='+this.pagina+'&tamanho='+this.tamanho+'&situacao='+this.situacao+(this.turmaId?'&turmaId='+this.turmaId:'')+this.sort.query());if(visit!==this.visit)return;this.provas.set(p.itens);this.total=p.total;this.pagina=p.pagina;}catch{if(visit===this.visit){this.provas.set([]);this.total=0;this.erro.set('Não foi possível carregar as provas para correção.');}}finally{if(visit===this.visit)this.loading.set(false);}}
 filter(){this.pagina=1;void this.load();}changePage(page:number){this.pagina=page;void this.load();}changeSize(size:number){this.tamanho=size;this.filter();}
}

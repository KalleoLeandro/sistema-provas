import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AlertComponent, ButtonDirective, CardComponent, RecordNameDirective } from '../../../design-system/components';
import { ProgressComponent } from '../../../design-system/progress';
import { PaginationComponent } from '../../../design-system/pagination';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { ApiService } from '../../../services/api.service';
import { Pagina } from '../../../models/aluno';
export interface ProvaDisponivel {id:number;titulo:string;materia:string;professor:string;disponivelEm:string;encerraEm:string;situacao:string;duracaoMinutos:number;tokenConfigurado:boolean;}
export interface ProvasAluno {provas:Pagina<ProvaDisponivel>;total:number;concluidas:number;}
@Component({selector:'app-provas-aluno',imports:[RouterLink,DatePipe,AlertComponent,ButtonDirective,CardComponent,RecordNameDirective,ProgressComponent,PaginationComponent,TableScrollComponent],templateUrl:'./provas.html'})
export class ProvasAlunoComponent implements OnInit,OnDestroy {
  private readonly api=inject(ApiService);
  readonly dados=signal<ProvasAluno|null>(null);
  readonly loading=signal(false);
  readonly erro=signal('');
  pagina=1;tamanho=15;
  private visit=0;
  private timer?:ReturnType<typeof setInterval>;
  ngOnInit(){void this.load();this.timer=setInterval(()=>{if(!this.loading())void this.load();},30000);}
  ngOnDestroy(){this.visit++;clearInterval(this.timer);}
  /** Consulta a identidade da sessão; o navegador não escolhe o aluno destinatário. */
  async load(){
    const visit=++this.visit;this.loading.set(true);this.erro.set('');
    try{const dados=await this.api.get<ProvasAluno>('/aluno/provas?pagina='+this.pagina+'&tamanho='+this.tamanho);
      if(visit!==this.visit)return;this.dados.set(dados);this.pagina=dados.provas.pagina;
    }catch{if(visit===this.visit){this.dados.set(null);this.erro.set('Não foi possível carregar suas provas. Tente novamente.');}}
    finally{if(visit===this.visit)this.loading.set(false);}
  }
  changePage(page:number){this.pagina=page;void this.load();}
  changeSize(size:number){this.tamanho=size;this.pagina=1;void this.load();}
}

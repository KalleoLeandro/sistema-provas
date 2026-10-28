import { ListSort } from '../../../design-system/list-sort';
import { SortHeaderComponent } from '../../../design-system/sort-header';
import { HttpErrorResponse } from '@angular/common/http';
import { ConfirmDialogComponent } from '../../../design-system/confirm-dialog';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { Turma } from '../../../models/turma';
import {
  AlertComponent,
  ButtonDirective,
  CardComponent,
  RecordNameDirective,
} from '../../../design-system/components';
@Component({
  selector: 'app-listar-turmas',
  imports: [SortHeaderComponent,ConfirmDialogComponent,RouterLink, AlertComponent, ButtonDirective, CardComponent, RecordNameDirective],
  templateUrl: './listar.html',
})
export class ListarTurmasComponent implements OnInit {
 readonly sort=new ListSort();
 get turmasOrdenadas(){return this.sort.ordered(this.turmas(),(t,c)=>c==='ensino'?(t.ensino==='F'?'Fundamental':'Médio'):[t.ano,t.letra]);}

  private readonly api = inject(ApiService);
  readonly turmas = signal<Turma[]>([]);
  readonly loading = signal(true);
  readonly erro = signal('');
  readonly deleteTarget=signal<Turma|null>(null);readonly deleting=signal(false);readonly actionError=signal('');
  confirmDelete(turma:Turma){this.actionError.set('');this.deleteTarget.set(turma);}
  closeDelete(){if(!this.deleting())this.deleteTarget.set(null);}
  async remove(){const turma=this.deleteTarget();if(!turma||this.deleting())return;this.deleting.set(true);this.actionError.set('');
    try{await this.api.delete('/turmas/'+turma.id);this.turmas.update(items=>items.filter(t=>t.id!==turma.id));this.deleteTarget.set(null);}
    catch(e){this.actionError.set(e instanceof HttpErrorResponse&&e.error?.mensagem?e.error.mensagem:'Não foi possível excluir a turma.');}finally{this.deleting.set(false);}}
  ngOnInit() {
    void this.load();
  }
  /** Carrega as turmas para conferência do cadastro realizado. */
  async load() {
    /* Uma lista vazia representa ausência de cadastro; falha de consulta permite nova tentativa. */
    this.loading.set(true);
    this.erro.set('');
    try {
      this.turmas.set(await this.api.get<Turma[]>('/turmas'));
    } catch {
      this.erro.set('Não foi possível carregar as turmas. Tente novamente.');
    } finally {
      this.loading.set(false);
    }
  }
}

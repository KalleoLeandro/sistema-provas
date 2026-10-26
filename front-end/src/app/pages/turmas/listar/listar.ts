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
  imports: [RouterLink, AlertComponent, ButtonDirective, CardComponent, RecordNameDirective],
  templateUrl: './listar.html',
})
export class ListarTurmasComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly turmas = signal<Turma[]>([]);
  readonly loading = signal(true);
  readonly erro = signal('');
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

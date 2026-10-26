import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ConfirmDialogComponent } from '../../../design-system/confirm-dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { Materia } from '../../../models/materia';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import {
  AlertComponent,
  ButtonDirective,
  CardComponent,
  RecordNameDirective,
  InputDirective,
} from '../../../design-system/components';

const normalize = (value: string) =>
  value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR');

@Component({
  selector: 'app-listar-materias',
  imports: [
    FormsModule,
    RouterLink,
    AlertComponent,
    ButtonDirective,
    CardComponent,
    RecordNameDirective,
    InputDirective,
    ConfirmDialogComponent,
  ],
  templateUrl: './listar.html',
})
export class ListarMateriasComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly materias = signal<Materia[]>([]);
  readonly search = signal('');
  readonly loading = signal(true);
  readonly erro = signal('');
  readonly actionError = signal('');
  readonly mensagem = signal('');
  readonly deleteTarget = signal<Materia | null>(null);
  readonly deleting = signal(false);
  confirmDelete(materia: Materia) {
    this.actionError.set('');
    this.mensagem.set('');
    this.deleteTarget.set(materia);
  }
  closeDelete() {
    if (!this.deleting()) this.deleteTarget.set(null);
  }
  async remove() {
    const materia = this.deleteTarget();
    if (!materia || this.deleting()) return;
    this.deleting.set(true);
    this.actionError.set('');
    try {
      await this.api.delete('/materias/' + materia.id);
      this.materias.update((items) => items.filter((item) => item.id !== materia.id));
      this.deleteTarget.set(null);
      this.mensagem.set('Matéria excluída.');
    } catch (error) {
      this.actionError.set(
        error instanceof HttpErrorResponse && error.error?.mensagem
          ? error.error.mensagem
          : 'Não foi possível excluir a matéria. Tente novamente.',
      );
    } finally {
      this.deleting.set(false);
    }
  }
  readonly filtered = computed(() => {
    const query = normalize(this.search().trim());
    return this.materias().filter((materia) => normalize(materia.nome).includes(query));
  });
  ngOnInit() {
    void this.load();
  }
  async load() {
    this.loading.set(true);
    this.erro.set('');
    try {
      this.materias.set(await this.api.get<Materia[]>('/materias'));
    } catch {
      this.erro.set('Não foi possível carregar as matérias. Tente novamente.');
    } finally {
      this.loading.set(false);
    }
  }
}

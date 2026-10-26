import { Component, HostListener, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { Materia } from '../../../models/materia';
import { ApiService } from '../../../services/api.service';
import {
  AlertComponent,
  ButtonDirective,
  CardComponent,
  FieldComponent,
  InputDirective,
} from '../../../design-system/components';
@Component({
  selector: 'app-editar-materia',
  imports: [
    FormsModule,
    RouterLink,
    AlertComponent,
    ButtonDirective,
    CardComponent,
    FieldComponent,
    InputDirective,
  ],
  templateUrl: './editar.html',
})
export class EditarMateriaComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  nome = '';
  readonly loading = signal(true);
  readonly loaded = signal(false);
  readonly busy = signal(false);
  readonly erro = signal('');
  async ngOnInit() {
    try {
      if (!Number.isSafeInteger(this.id) || this.id < 1) throw new Error();
      const materia = await this.api.get<Materia>('/materias/' + this.id);
      this.nome = materia.nome;
      this.loaded.set(true);
    } catch (error) {
      this.erro.set(
        error instanceof HttpErrorResponse && error.error?.mensagem
          ? error.error.mensagem
          : 'Não foi possível carregar a matéria.',
      );
    } finally {
      this.loading.set(false);
    }
  }
  async save(form: NgForm) {
    if (this.busy() || !this.loaded()) return;
    if (form.invalid || !this.nome.trim()) {
      form.control.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.erro.set('');
    try {
      await this.api.put('/materias/' + this.id, { nome: this.nome.trim() });
      await this.router.navigateByUrl('/cadastro/materias/listar');
    } catch (error) {
      this.erro.set(
        error instanceof HttpErrorResponse && error.error?.mensagem
          ? error.error.mensagem
          : 'Não foi possível salvar a matéria. Tente novamente.',
      );
    } finally {
      this.busy.set(false);
    }
  }
  @HostListener('window:pagehide') clear() {
    this.nome = '';
  }
  ngOnDestroy() {
    this.clear();
  }
}

import { AllowedValuesDirective, ValidationMessagePipe } from '../../../design-system/validators';
import { Component, ElementRef, HostListener, OnDestroy, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import {
  AlertComponent,
  CardComponent,
  FieldComponent,
  InputDirective,
} from '../../../design-system/components';
import { FormActionsComponent } from '../../../design-system/form-actions';

@Component({
  selector: 'app-cadastrar-turma',
  imports: [AllowedValuesDirective, ValidationMessagePipe, 
    FormsModule,
    AlertComponent,
    CardComponent,
    FieldComponent,
    InputDirective,
    FormActionsComponent,
  ],
  templateUrl: './cadastrar.html',
})
export class CadastrarTurmaComponent implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly element = inject(ElementRef<HTMLElement>);
  readonly anos = Array.from({ length: 9 }, (_, index) => index + 1);
  readonly letras = Array.from({ length: 26 }, (_, index) => String.fromCharCode(65 + index));
  ensino = '';
  ano: number | null = null;
  letra = '';
  readonly busy = signal(false);
  readonly erro = signal('');
  readonly mensagem = signal('');

  /** Limpa a seleção e os avisos para iniciar um novo cadastro. */
  reset(form: NgForm) {
    /* Não permite substituir os dados enquanto uma gravação está em andamento. */
    if (this.busy()) return;
    this.clear();
    form.resetForm({ ensino: '', ano: null, letra: '' });
    this.erro.set('');
    this.mensagem.set('');
  }

  /**
   * Cadastra a turma e define o próximo passo do formulário.
   * @param form Estado do formulário de cadastro.
   * @param continuar Indica se o usuário deseja cadastrar outra turma na mesma página.
   */
  async save(form: NgForm, continuar = false) {
    /* Exige os três dados de identificação e evita o envio repetido por duplo clique. */
    if (this.busy()) return;
    if (form.invalid || !this.ensino || this.ano === null || !this.letra) {
      form.control.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.erro.set('');
    this.mensagem.set('');
    try {
      await this.api.post('/turmas', { ensino: this.ensino, ano: this.ano, letra: this.letra });
      /*
       * Após gravar, limpa as seleções. Cadastrar retorna à lista; cadastrar e
       * continuar mantém a página disponível para uma nova turma independente.
       */
      this.clear();
      form.resetForm({ ensino: '', ano: null, letra: '' });
      this.mensagem.set('Turma cadastrada com sucesso.');
      if (continuar)
        queueMicrotask(() => this.element.nativeElement.querySelector('select')?.focus());
      else await this.router.navigateByUrl('/cadastro/turmas/listar');
    } catch (error) {
      /* Preserva a seleção na falha para permitir corrigir os dados ou tentar novamente. */
      this.erro.set(
        error instanceof HttpErrorResponse && error.error?.mensagem
          ? error.error.mensagem
          : 'Não foi possível cadastrar a turma. Tente novamente.',
      );
    } finally {
      this.busy.set(false);
    }
  }

  /** Descarta seleções ao sair da página, sem persistir dados do formulário. */
  @HostListener('window:pagehide')
  clear() {
    this.ensino = '';
    this.ano = null;
    this.letra = '';
  }
  ngOnDestroy() {
    this.clear();
  }
}

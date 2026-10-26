import { NotBlankDirective, ValidationMessagePipe } from '../../../design-system/validators';
import { Component, HostListener, OnDestroy, inject, signal, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormActionsComponent } from '../../../design-system/form-actions';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { AlertComponent, CardComponent, FieldComponent, InputDirective } from '../../../design-system/components';

@Component({
  selector: 'app-cadastrar-materia',
  imports: [NotBlankDirective, ValidationMessagePipe, FormsModule, AlertComponent, CardComponent, FieldComponent, InputDirective, FormActionsComponent],
  templateUrl: './cadastrar.html',
  styles: [`h2{font-size:28px;margin:10px 0 24px}`],
})
export class CadastrarMateriaComponent implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly element = inject(ElementRef<HTMLElement>);
  nome = '';
  readonly busy = signal(false);
  readonly erro = signal('');
  readonly mensagem = signal('');

  reset(form: NgForm) {
    if (this.busy()) return;
    this.clear(); form.resetForm({ nome: '' }); this.erro.set(''); this.mensagem.set('');
  }

  async save(form: NgForm, continuar = false) {
    if (this.busy()) return;
    if (form.invalid || !this.nome.trim()) { form.control.markAllAsTouched(); return; }
    this.busy.set(true); this.erro.set(''); this.mensagem.set('');
    try {
      await this.api.post('/materias', { nome: this.nome.trim() });
      this.clear(); form.resetForm({ nome: '' });
      this.mensagem.set('Matéria cadastrada com sucesso.');
      if (continuar) {
        queueMicrotask(() => this.element.nativeElement.querySelector('input')?.focus());
      } else {
        await this.router.navigateByUrl('/cadastro/materias/listar');
      }
    } catch (error) {
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem ? error.error.mensagem
        : error instanceof HttpErrorResponse && error.status === 401 ? 'Sua sessão expirou. Entre novamente.'
        : 'Não foi possível cadastrar a matéria. Tente novamente.');
    } finally { this.busy.set(false); }
  }
  @HostListener('window:pagehide')
  clear() { this.nome = ''; }
  ngOnDestroy() { this.clear(); }
}

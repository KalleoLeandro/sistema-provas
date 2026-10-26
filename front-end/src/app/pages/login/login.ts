import { Component, HostListener, OnDestroy, inject, ChangeDetectorRef, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule, NgForm } from '@angular/forms';
import { AlertComponent, ButtonDirective, FieldComponent, InputDirective, CardComponent } from '../../design-system/components';
import { NotBlankDirective, TrimmedMinlengthDirective, ValidationMessagePipe } from '../../design-system/validators';
type Perfil = 'aluno' | 'professor' | 'administrador';
@Component({
 selector: 'app-login',
 imports: [FormsModule, AlertComponent, ButtonDirective, FieldComponent, InputDirective, CardComponent, NotBlankDirective, TrimmedMinlengthDirective, ValidationMessagePipe],
 templateUrl: './login.html'
})
export class LoginComponent implements OnDestroy {
 private readonly auth = inject(AuthService);
 private readonly router = inject(Router);
 private readonly changeDetector = inject(ChangeDetectorRef);
 private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
 readonly perfis: { valor: Perfil; rotulo: string }[] = [
  { valor: 'aluno', rotulo: 'Aluno' },
  { valor: 'professor', rotulo: 'Professor' },
  { valor: 'administrador', rotulo: 'Administrador' }
 ];
 perfil: Perfil = 'aluno';
 usuario = ''; senha = ''; cgm = ''; mensagem = ''; ocupado = false;
 selecionarPerfil(perfil: Perfil, formulario: NgForm) {
  if (this.ocupado || this.perfil === perfil) return;
  this.perfil = perfil;
  this.limparCampos();
  formulario.resetForm({ usuario: '', senha: '', cgm: '' });
  this.mensagem = '';
 }
 /** Valida a entrada e envia as credenciais somente quando todos os campos estão válidos. */
 async entrar(formulario: NgForm, event?: Event) {
  if (this.ocupado) return;
  /* Autofill pode preencher o DOM sem emitir input; sincroniza os campos antes de validar. */
  if (event?.target instanceof HTMLFormElement) {
   for (const campo of ['usuario', 'senha', 'cgm'] as const) {
    const input = event.target.elements.namedItem(campo);
    const control = formulario.controls[campo];
    if (input instanceof HTMLInputElement && control) {
     this[campo] = input.value;
     control.setValue(input.value);
    }
   }
  }
  /* Um envio inválido apresenta os motivos e leva o foco ao primeiro campo pendente. */
  if (formulario.invalid) {
   formulario.control.markAllAsTouched();
   this.mensagem = 'Revise os campos destacados para entrar.';
   this.changeDetector.markForCheck();
   queueMicrotask(() => this.element.nativeElement.querySelector<HTMLInputElement>('.campos input.ng-invalid')?.focus());
   return;
  }
  if (this.perfil !== 'aluno' && !this.usuario.trim()) {
   this.mensagem = 'Informe seu login.';
   return;
  }
  this.mensagem = '';
  this.ocupado = true;
  try {
   await this.auth.login(this.perfil==='aluno'?this.cgm.trim():this.usuario.trim(), this.perfil==='aluno'?'':this.senha, this.perfil);
   await this.router.navigateByUrl(this.perfil==='aluno'?'/aluno/provas':'/dashboard');
  } catch (error) {
   this.mensagem = error instanceof HttpErrorResponse && error.error?.mensagem
    ? error.error.mensagem : 'Não foi possível entrar. Tente novamente.';
  } finally {
   this.limparCampos();
   formulario.resetForm({ usuario: '', senha: '', cgm: '' });
   this.ocupado = false;
   this.changeDetector.markForCheck();
  }
 }
 @HostListener('window:pagehide')
 limparCampos() { this.usuario = ''; this.senha = ''; this.cgm = ''; }
 ngOnDestroy() { this.limparCampos(); }
}


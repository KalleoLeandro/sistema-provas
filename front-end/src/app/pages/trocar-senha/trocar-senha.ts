import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AlertComponent, ButtonDirective, InputDirective, CardComponent } from '../../design-system/components';
import { AuthService } from '../../services/auth.service';
@Component({selector:'app-trocar-senha',imports:[FormsModule, AlertComponent, ButtonDirective, InputDirective, CardComponent],template: `
<main class="pagina-entrada"><visual-cartao style="display:block;max-width:32rem;margin:3rem auto;padding:2rem">
<h1 id="titulo">Defina sua nova senha</h1>
<p>Após o cadastro ou redefinição, troque sua senha para continuar.</p>
@if(mensagem){<visual-aviso>{{ mensagem }}</visual-aviso>}
<form #form="ngForm" (ngSubmit)="salvar(form)">
<p><label>Senha atual <input campoVisual type="password" name="atual" [(ngModel)]="atual" required maxlength="128" autocomplete="current-password"></label></p>
<p><label>Nova senha <input campoVisual type="password" name="nova" [(ngModel)]="nova" required minlength="8" maxlength="128" autocomplete="new-password"></label></p>
<p>Use de 8 a 128 caracteres e uma senha diferente da atual.</p>
<p><label>Confirme a nova senha <input campoVisual type="password" name="confirmacao" [(ngModel)]="confirmacao" required maxlength="128" autocomplete="new-password"></label></p>
<div class="form-actions">
<button botaoVisual type="submit" [disabled]="ocupado">Salvar nova senha</button>
<button botaoVisual variant="secondary" type="button" [disabled]="ocupado" (click)="sair()">Sair</button>
</div>
</form></visual-cartao></main>`})
export class TrocarSenhaComponent {
 private readonly changeDetector = inject(ChangeDetectorRef);
 private readonly auth=inject(AuthService); private readonly router=inject(Router);
 atual=''; nova=''; confirmacao=''; mensagem=''; ocupado=false;
 async salvar(form:NgForm) {
  if(this.ocupado) return;
  if(form.invalid || !this.nova.trim()) {form.control.markAllAsTouched();this.mensagem='Preencha os campos. A nova senha deve conter de 8 a 128 caracteres.';return;}
  if(this.nova!==this.confirmacao) {this.mensagem='A confirmação não corresponde à nova senha.';return;}
  this.ocupado=true;this.mensagem='';
  try {await this.auth.trocarSenha(this.atual,this.nova);this.atual=this.nova=this.confirmacao='';await this.router.navigateByUrl('/login');}
  catch(e) {this.mensagem=e instanceof HttpErrorResponse && e.error?.mensagem ? e.error.mensagem : 'Não foi possível trocar a senha. Tente novamente.';}
  finally {this.ocupado=false;this.changeDetector.markForCheck();}
 }
 async sair() {try {await this.auth.logout();await this.router.navigateByUrl('/login');} catch {this.mensagem='Não foi possível sair. Tente novamente.';this.changeDetector.markForCheck();}}
}

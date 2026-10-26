import { Component, OnInit, OnDestroy, HostListener, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective } from '../../../design-system/components';

@Component({
  selector: 'app-editar-usuario',
  imports: [FormsModule, RouterLink, AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective],
  templateUrl: './editar.html',
  styles: [`h2{margin:0 0 12px;font-size:28px}p{color:var(--cor-texto-suave)}visual-cartao{display:block;margin-top:20px}a{color:var(--cor-primaria)}`],
})
export class EditarUsuarioComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  login = ''; senha = '';
  readonly busy = signal(false);
  readonly loading = signal(true);
  readonly loaded = signal(false);
  readonly erro = signal('');

  async ngOnInit() {
    try {
      if (!Number.isSafeInteger(this.id) || this.id < 1) throw new Error();
      const usuario = await this.api.get<{ login: string }>('/administradores/' + this.id);
      this.login = usuario.login; this.loaded.set(true);
    } catch { this.erro.set('Não foi possível carregar o usuário.'); }
    finally { this.loading.set(false); }
  }

  async save(form: NgForm) {
    if (this.busy() || !this.loaded()) return;
    if (form.invalid || this.login.trim().length < 3) { form.control.markAllAsTouched(); return; }
    this.busy.set(true); this.erro.set('');
    try {
      await this.api.put('/administradores/' + this.id, { login: this.login.trim(), senha: this.senha || null });
      if (this.auth.usuario()?.id === this.id) await this.auth.restore();
      await this.router.navigateByUrl('/cadastro/usuarios/listar');
    } catch (error) {
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem ? error.error.mensagem : 'Não foi possível salvar as alterações.');
    } finally { this.senha = ''; this.busy.set(false); }
  }
  @HostListener('window:pagehide')
  clear() { this.login = ''; this.senha = ''; }
  ngOnDestroy() { this.clear(); }
}

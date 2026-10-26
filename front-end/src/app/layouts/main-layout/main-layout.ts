import { CdkScrollable } from '@angular/cdk/scrolling';
import { Component, HostListener, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ButtonDirective } from '../../design-system/components';
import { AdminMenuComponent } from './admin-menu';
import { ProfessorMenuComponent } from './professor-menu';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  imports: [CdkScrollable, RouterLink, RouterLinkActive, RouterOutlet, AdminMenuComponent, ProfessorMenuComponent, ButtonDirective],
  templateUrl: './main-layout.html',
  styleUrl: '../../design-system/portal.css',
})
export class MainLayoutComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly saindo = signal(false);
  readonly erro = signal('');
  async logout() {
    if (this.saindo()) return;
    this.saindo.set(true);
    this.erro.set('');
    try { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
    catch { this.erro.set('Não foi possível sair. Tente novamente.'); }
    finally { this.saindo.set(false); }
  }
  readonly menuAberto = signal(false);

  fecharMenu() { this.menuAberto.set(false); }

  @HostListener('document:keydown.escape')
  fecharComTeclado() { this.fecharMenu(); }
}

import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonDirective, CardComponent } from '../../design-system/components';
@Component({
  selector: 'app-indisponivel',
  imports: [ButtonDirective, CardComponent],
  styleUrl: './indisponivel.css',
  templateUrl: './indisponivel.html',
})
export class IndisponivelComponent {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly busy = signal(false);
  async retry() {
    if (this.busy()) return;
    this.busy.set(true);
    const target = this.route.snapshot.queryParamMap.get('retorno');
    try {
      await this.router.navigateByUrl(
        target?.startsWith('/') && !target.startsWith('//') && !target.startsWith('/indisponivel')
          ? target
          : '/dashboard',
      );
    } finally {
      this.busy.set(false);
    }
  }
}

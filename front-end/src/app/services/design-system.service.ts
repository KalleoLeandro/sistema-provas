import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from './api.service';

/** Consulta a permissão do catálogo; a autorização dos arquivos pertence ao servidor. */
@Injectable({ providedIn: 'root' })
export class DesignSystemService {
  private readonly api = inject(ApiService);
  readonly allowed = signal(false);

  /**
   * Confirma se a sessão atual pertence à conta master ativa.
   * @returns Permissão confirmada pela API.
   * @throws Erro de comunicação quando não é possível confirmar a permissão.
   */
  async checkAccess(): Promise<boolean> {
    /* A interface não deduz a permissão pelo nome ou login e não a persiste no navegador. */
    this.allowed.set(false);
    try {
      await this.api.get<void>('/design-system/acesso');
      this.allowed.set(true);
      return true;
    } catch (error) {
      /* Falhas ocultam o menu; somente 401 e 403 são respostas definitivas de acesso recusado. */
      this.allowed.set(false);
      if (error instanceof HttpErrorResponse && [401, 403].includes(error.status)) return false;
      throw error;
    }
  }
}

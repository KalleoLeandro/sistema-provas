import { inject, Injectable } from '@angular/core';
import { NzNotificationService } from 'ng-zorro-antd/notification';
/**
 * Padroniza as notificações de resultado das operações no portal e no catálogo.
 * A página define a mensagem e o tipo; esta service apenas apresenta o feedback,
 * sem decidir se a operação foi concluída nem executar alterações no cadastro.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly notification = inject(NzNotificationService);
  /**
   * Apresenta uma notificação temporária do resultado de uma operação.
   *
   * @param message Mensagem definida pelo fluxo chamador.
   * @param type Tipo visual da notificação; informativo por padrão.
   * @param title Título exibido; ESFINORA por padrão.
   * @returns Referência da notificação criada.
   */
  show(message: string, type: 'success' | 'info' | 'warning' | 'error' = 'info', title = 'ESFINORA') {
    /*
     * O feedback permanece visível por cinco segundos. Exibir uma notificação
     * não conclui nem desfaz a operação: o resultado de negócio já deve ter
     * sido determinado pela página e pela API antes desta chamada.
     */
    return this.notification.create(type, title, message, { nzDuration: 5000 });
  }
}

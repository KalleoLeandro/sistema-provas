import { Component, Directive, Input } from '@angular/core';

@Directive({ selector: 'input[campoVisual], textarea[campoVisual], select[campoVisual]', host: { autocomplete: 'off', autocapitalize: 'none', spellcheck: 'false', 'data-lpignore': 'true', 'data-1p-ignore': 'true', class: 'campo-visual' } })
export class InputDirective { }

@Directive({ selector: 'button[botaoVisual], a[botaoVisual]', host: {
  class: 'botao-visual', '[class.botao-secundario]': 'variant === "secondary"',
  '[class.botao-editar]': 'variant === "edit"', '[class.botao-perigo]': 'variant === "danger"',
  '[class.botao-sucesso]': 'variant === "success"', '[class.botao-continuar]': 'variant === "continue"',
  '[class.botao-compacto]': 'size === "compact"',
} })
export class ButtonDirective {
  @Input() variant: 'primary' | 'secondary' | 'edit' | 'danger' | 'success' | 'continue' = 'primary';
  @Input() size: 'default' | 'compact' = 'default';
}

@Component({ selector: 'visual-campo', templateUrl: './field.html', styleUrl: './field.css' })
export class FieldComponent {
  @Input({ required: true }) rotulo = '';
  @Input({ required: true }) identificador = '';
  @Input() orientacao = '';
  @Input() erro = '';
}
@Component({ selector: 'visual-cartao', template: '<ng-content />', styleUrl: './card.css', host: { '[class.compact]': 'density === "compact"' } })
export class CardComponent { @Input() density: 'default' | 'compact' = 'default'; }
@Component({ selector: 'visual-aviso', template: '<ng-content />', host: {
  '[attr.role]': 'variant === "success" ? "status" : "alert"',
  '[class.aviso-sucesso]': 'variant === "success"',
}, styleUrl: './alert.css' })
export class AlertComponent { @Input() variant: 'error' | 'success' = 'error'; }

/** Destaca o nome que identifica um registro nas listas. */
@Directive({ selector: 'th[nomeRegistro], td[nomeRegistro]', host: { class: 'registro-nome' } })
export class RecordNameDirective { }

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ButtonDirective } from '../components';
import { NzIconModule } from 'ng-zorro-antd/icon';

@Component({
  selector: 'visual-fieldset',
  template: '<fieldset class="ds-fieldset"><legend>{{label}}</legend><ng-content /></fieldset>',
})
export class FieldsetComponent {
  @Input({ required: true }) label = '';
}
@Component({
  selector: 'visual-section-title',
  template:
    '<header>@if (eyebrow) {<span class="sobretitulo">{{eyebrow}}</span>}<h2 class="ds-title">{{title}}</h2>@if (description) {<p class="ds-muted">{{description}}</p>}<ng-content /></header>',
})
export class SectionTitleComponent {
  @Input({ required: true }) title = '';
  @Input() eyebrow = '';
  @Input() description = '';
}
@Component({
  selector: 'visual-footer',
  template:
    '<footer class="ds-footer"><span>{{brand}}</span><span>{{message}}</span><ng-content /></footer>',
})
export class FooterComponent {
  @Input() brand = 'ESFINORA';
  @Input() message = 'Aprender transforma.';
}
@Component({
  selector: 'visual-navbar',
  template:
    '<header class="ds-navbar"><strong>{{brand}}</strong><div class="ds-row"><ng-content /></div></header>',
})
export class NavbarComponent {
  @Input() brand = 'ESFINORA';
}
@Component({
  selector: 'visual-read-only',
  template: '<dl class="ds-readonly"><dt>{{label}}</dt><dd>{{value}}</dd></dl>',
})
export class ReadOnlyComponent {
  @Input({ required: true }) label = '';
  @Input() value: string | number = '';
}
@Component({
  selector: 'visual-content-line',
  template:
    '<div class="ds-panel ds-row"><strong>{{label}}</strong><span>{{value}}</span><ng-content /></div>',
})
export class ContentLineComponent {
  @Input({ required: true }) label = '';
  @Input() value = '';
}
@Component({
  selector: 'visual-modular-field',
  template:
    '<div class="ds-row"><span>{{prefix}}</span><ng-content /><span>{{suffix}}</span></div>',
})
export class ModularFieldComponent {
  @Input() prefix = '';
  @Input() suffix = '';
}
@Component({
  selector: 'visual-icon-action',
  imports: [ButtonDirective, NzIconModule],
  template:
    '<button botaoVisual variant="edit" size="compact" type="button" [disabled]="disabled" [attr.aria-label]="label" (click)="action.emit()"><nz-icon [nzType]="icon" aria-hidden="true" /></button>',
})
export class IconActionComponent {
  @Input({ required: true }) label = '';
  @Input() icon = 'edit';
  @Input() disabled = false;
  @Output() action = new EventEmitter<void>();
}

import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  ViewChild,
} from '@angular/core';
import { ButtonDirective } from './components';
let nextId = 0;
@Component({
  selector: 'visual-confirm-dialog',
  imports: [ButtonDirective],
  styleUrl: './confirm-dialog.css',
  template: ` <dialog
    #dialog
    role="alertdialog"
    [class.with-close]="showClose"
    [attr.aria-labelledby]="id + '-title'"
    [attr.aria-describedby]="id + '-message'"
    [attr.aria-busy]="busy"
    (cancel)="cancel($event)"
  >
    @if(showClose){<button class="dialog-close" type="button" aria-label="Fechar" [disabled]="busy" (click)="cancel()">×</button>}
    <h2 [id]="id + '-title'">{{ title }}</h2>
    <p [id]="id + '-message'">{{ message }}</p>
    <ng-content />
    <div class="form-actions form-actions-end">
      @if(showCancel){<button
        botaoVisual
        variant="secondary"
        type="button"
        autofocus
        [disabled]="busy"
        (click)="cancel()"
      >
        {{ cancelLabel }}</button>}<button
        botaoVisual
        [variant]="confirmVariant"
        type="button"
        [disabled]="busy"
        (click)="confirmed.emit()"
      >
        {{ busy ? busyLabel : confirmLabel }}
      </button>
    </div>
  </dialog>`,
})
export class ConfirmDialogComponent implements AfterViewInit, OnChanges {
  @Input() open = false;
  @Input() busy = false;
  @Input() title = 'Excluir registro';
  @Input() message = '';
  @Input() cancelLabel='Cancelar';
  @Input() showCancel=true;
  @Input() showClose=false;
  @Input() confirmLabel='Excluir';
  @Input() busyLabel='Excluindo…';
  @Input() confirmVariant:'primary'|'danger'='danger';
  @Output() openChange = new EventEmitter<boolean>();
  @Output() confirmed = new EventEmitter<void>();
  @ViewChild('dialog', { static: true }) dialog?: ElementRef<HTMLDialogElement>;
  readonly id = 'confirm-dialog-' + nextId++;
  ngAfterViewInit() {
    this.sync();
  }
  ngOnChanges() {
    this.sync();
  }
  private sync() {
    const dialog = this.dialog?.nativeElement;
    if (!dialog) return;
    if (this.open && !dialog.open) {
      if (dialog.showModal) dialog.showModal();
      else dialog.setAttribute('open', '');
    } else if (!this.open && dialog.open) {
      if (dialog.close) dialog.close();
      else dialog.removeAttribute('open');
    }
  }
  cancel(event?: Event) {
    event?.preventDefault();
    if (!this.busy) this.openChange.emit(false);
  }
}

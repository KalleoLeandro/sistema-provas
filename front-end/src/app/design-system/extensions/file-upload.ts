import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { ButtonDirective, InputDirective } from '../components';
@Component({
  selector: 'visual-file-upload',
  imports: [ButtonDirective, InputDirective],
  template: `<div class="ds-stack">
    <label
      >{{ label
      }}<input
        campoVisual
        type="file"
        [accept]="accept"
        [multiple]="multiple"
        [disabled]="disabled"
        (change)="select($event)"
    /></label>
    <ul>
      @for (file of files(); track $index) {
        <li class="ds-row">
          {{ file.name }} ({{ file.size }} bytes)<button
            botaoVisual
            size="compact"
            variant="danger"
            type="button"
            [disabled]="disabled"
            [attr.aria-label]="'Remover ' + file.name"
            (click)="remove($index)"
          >
            Remover
          </button>
        </li>
      }
    </ul>
    <p role="status">{{ files().length }} arquivo(s) selecionado(s).</p>
  </div>`,
})
export class FileUploadComponent {
  @Input() label = 'Selecionar arquivos';
  @Input() accept = '';
  @Input() multiple = true;
  @Input() disabled = false;
  @Output() filesChange = new EventEmitter<File[]>();
  readonly files = signal<File[]>([]);
  select(event: Event) {
    if (this.disabled) return;
    const input = event.target as HTMLInputElement;
    const selected = Array.from(input.files ?? []);
    this.files.set(this.multiple ? [...this.files(), ...selected] : selected.slice(0, 1));
    this.filesChange.emit(this.files());
    input.value = '';
  }
  remove(index: number) {
    if (this.disabled) return;
    this.files.update((files) => files.filter((_, i) => i !== index));
    this.filesChange.emit(this.files());
  }
}

import { Component, Input, inject, OnChanges, OnDestroy } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { InputDirective } from '../components';
@Component({
  selector: 'visual-pdf-viewer',
  imports: [InputDirective],
  template:
    '<div class="ds-stack"><label>Selecionar PDF<input campoVisual type="file" accept="application/pdf,.pdf" (change)="select($event)" /></label>@if (error) {<p role="alert">{{error}}</p>}@if (safeUrl) {<iframe [src]="safeUrl" title="Visualizador de PDF" width="100%" height="480"></iframe><a [href]="downloadUrl" target="_blank" rel="noopener">Abrir PDF em outra aba</a>}</div>',
})
export class PdfViewerComponent implements OnChanges, OnDestroy {
  @Input() src = '';
  private sanitizer = inject(DomSanitizer);
  private objectUrl = '';
  safeUrl: SafeResourceUrl | null = null;
  downloadUrl = '';
  error = '';
  ngOnChanges() {
    this.reset();
    if (!this.src) return;
    let url: URL;
    try {
      url = new URL(this.src, document.baseURI);
    } catch {
      this.error = 'Endereço de PDF inválido.';
      return;
    }
    if (
      url.origin !== location.origin ||
      !/^https?:$/.test(url.protocol) ||
      !url.pathname.toLowerCase().endsWith('.pdf')
    ) {
      this.error = 'Informe um PDF hospedado no próprio portal.';
      return;
    }
    this.show(url.href);
  }
  select(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.reset();
    if (file && (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf'))) {
      this.objectUrl = URL.createObjectURL(new Blob([file], { type: 'application/pdf' }));
      this.show(this.objectUrl);
    } else if (file) this.error = 'Selecione um arquivo PDF.';
    input.value = '';
  }
  private show(url: string) {
    this.downloadUrl = url;
    this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
  private reset() {
    if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
    this.objectUrl = '';
    this.safeUrl = null;
    this.downloadUrl = '';
    this.error = '';
  }
  ngOnDestroy() {
    this.reset();
  }
}

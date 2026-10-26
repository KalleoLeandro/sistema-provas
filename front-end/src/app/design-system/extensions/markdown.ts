import { Component, computed, input } from '@angular/core';
import { marked } from 'marked';
/** O HTML resultante passa pelo sanitizador padrao do Angular. */
@Component({ selector: 'visual-markdown', template: '<article [innerHTML]="html()"></article>' })
export class MarkdownComponent {
  readonly source = input('');
  readonly html = computed(() => marked.parse(this.source(), { async: false }));
}

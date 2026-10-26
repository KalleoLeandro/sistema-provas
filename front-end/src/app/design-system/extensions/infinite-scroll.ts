import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  Output,
  EventEmitter,
  ViewChild,
} from '@angular/core';
import { ButtonDirective } from '../components';
@Component({
  selector: 'visual-infinite-scroll',
  imports: [ButtonDirective],
  template:
    '<ng-content /><div #sentinel aria-hidden="true"></div>@if (loading) {<p role="status">Carregando…</p>} @else if (hasMore) {<button botaoVisual variant="secondary" type="button" (click)="requestMore()">Carregar mais</button>} @else {<p role="status">Todos os registros foram carregados.</p>}',
})
export class InfiniteScrollComponent implements AfterViewInit, OnDestroy {
  @Input() loading = false;
  @Input() hasMore = true;
  @Output() loadMore = new EventEmitter<void>();
  @ViewChild('sentinel', { static: true }) sentinel!: ElementRef<HTMLElement>;
  private observer?: IntersectionObserver;
  private requested = false;
  ngAfterViewInit() {
    if (typeof IntersectionObserver === 'undefined') return;
    this.observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        if (!this.requested) this.requestMore();
        this.requested = true;
      } else this.requested = false;
    });
    this.observer.observe(this.sentinel.nativeElement);
  }
  requestMore() {
    if (!this.loading && this.hasMore) this.loadMore.emit();
  }
  ngOnDestroy() {
    this.observer?.disconnect();
  }
}

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonDirective, InputDirective } from './components';

@Component({
  selector: 'visual-pagination', imports: [FormsModule, ButtonDirective, InputDirective],
  templateUrl: './pagination.html', styleUrl: './pagination.css',
})
export class PaginationComponent {
  @Input() total = 0;
  @Input() page = 1;
  @Input() pageSize = 15;
  @Input() busy = false;
  @Input() inputId = 'paginacao-tamanho';
  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();
  readonly sizes = [15, 25, 50];
  get pages() { return Math.max(1, Math.ceil(this.total / this.pageSize)); }
  get start() { return this.total ? (this.page - 1) * this.pageSize + 1 : 0; }
  get end() { return Math.min(this.page * this.pageSize, this.total); }
}

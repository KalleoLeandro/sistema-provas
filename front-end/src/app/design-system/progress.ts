import { Component, Input } from '@angular/core';
@Component({selector:'visual-progress',templateUrl:'./progress.html',styleUrl:'./progress.css'})
export class ProgressComponent {
  @Input() total=0;
  @Input() completed=0;
  @Input() label='Progresso';
  get maximum(){return Math.max(0,Number.isFinite(this.total)?this.total:0);}
  get value(){return Math.max(0,Math.min(this.maximum,Number.isFinite(this.completed)?this.completed:0));}
  get percent(){return this.maximum?Math.round(this.value/this.maximum*100):0;}
}

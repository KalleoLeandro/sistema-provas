import { Component,OnInit,OnDestroy,signal } from '@angular/core';
import { CardComponent } from '../../design-system/components';
import { pensamentoDoDia } from './pensamentos';
@Component({selector:'app-daily-thought',imports:[CardComponent],templateUrl:'./daily-thought.html',styleUrl:'../../design-system/daily-thought.css'})
export class DailyThoughtComponent implements OnInit,OnDestroy {
 readonly pensamento=signal(pensamentoDoDia());private timer?:ReturnType<typeof setInterval>;
 ngOnInit(){this.timer=setInterval(()=>this.pensamento.set(pensamentoDoDia()),60000);}
 ngOnDestroy(){clearInterval(this.timer);}
}

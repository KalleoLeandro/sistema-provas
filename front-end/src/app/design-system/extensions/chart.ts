import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import Chart from 'chart.js/auto';
@Component({
  selector: 'visual-chart',
  templateUrl: './chart.html',
})
export class ChartComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() label = 'Resultado por matéria';
  @Input() labels = ['Português', 'Matemática', 'História'];
  @Input() values: (number|null)[] = [78, 85, 91];
  @Input() type: 'bar' | 'line' | 'pie' | 'doughnut' = 'bar';
  @Input() maximum?: number;
  @ViewChild('canvas', { static: true }) canvas!: ElementRef<HTMLCanvasElement>;
  private chart?: Chart;
  ngAfterViewInit() {
    this.render();
  }
  ngOnChanges() {
    if (this.chart) this.render();
  }
  private render() {
    this.chart?.destroy();
    const styles = getComputedStyle(this.canvas.nativeElement);
    const primary=styles.getPropertyValue('--cor-primaria').trim()||'#4099ff';
    const palette=['#4099ff','#2ed8b6','#ffb64d','#ff5370','#7759de','#00acc1','#90c26d','#e88754','#5476c9','#ce71c6'];
    this.chart = new Chart(this.canvas.nativeElement, {
      type: this.type,
      data: {
        labels: this.labels,
        datasets: [
          {
            label: this.label,
            data: this.values,
            backgroundColor: this.labels.map((_,i)=>i<palette.length?palette[i]:`hsl(${(i*137.508)%360} 48% 43%)`),
          },
        ],
      },
      options: { responsive: true, maintainAspectRatio: false, animation: false,font:{family:styles.getPropertyValue('--fonte-padrao').trim()},
        plugins:{legend:{display:this.type==='pie'||this.type==='doughnut',position:'bottom'}},
        ...(['bar','line'].includes(this.type)?{scales:{y:{beginAtZero:true,max:this.maximum}}}:{}),
      },
    });
  }
  ngOnDestroy() {
    this.chart?.destroy();
  }
}

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
    const primary=styles.getPropertyValue('--cor-primaria').trim()||'#007d9c';
    const palette=[primary,'#547c30','#66529c','#a95c23','#28628c','#a44265','#58735e','#8b6b19','#396b76','#775c3d'];
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
      options: { responsive: true, maintainAspectRatio: false, animation: false,
        plugins:{legend:{display:this.type==='pie'||this.type==='doughnut',position:'bottom'}},
        ...(['bar','line'].includes(this.type)?{scales:{y:{beginAtZero:true,max:this.maximum}}}:{}),
      },
    });
  }
  ngOnDestroy() {
    this.chart?.destroy();
  }
}

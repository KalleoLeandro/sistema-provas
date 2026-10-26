import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({ selector: 'visual-metric-card', imports: [RouterLink],
  templateUrl: './metric-card.html', styleUrl: './metric-card.css',
})
export class MetricCardComponent {
  @Input({ required: true }) value: number | string = 0;
  @Input({ required: true }) description = '';
  @Input({ required: true }) link = '';
  @Input() actionLabel = 'Ver detalhes';
}

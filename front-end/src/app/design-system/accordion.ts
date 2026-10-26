import { Component, Input } from '@angular/core';

@Component({ selector: 'visual-accordion', templateUrl: './accordion.html', styleUrl: './accordion.css' })
export class AccordionComponent {
  @Input({ required: true }) title = '';
  @Input() description = '';
}

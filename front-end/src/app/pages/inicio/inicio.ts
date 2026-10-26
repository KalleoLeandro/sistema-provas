import { Component } from '@angular/core';
import { CardComponent } from '../../design-system/components';

@Component({
  selector: 'app-inicio',
  imports: [CardComponent],
  templateUrl: './inicio.html',
  styles: [`h2 { margin: 0 0 12px; font-size: 24px; } p { margin: 0; color: var(--cor-texto-suave); }`],
})
export class InicioComponent {}

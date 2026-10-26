import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { CardComponent } from '../../design-system/components';

@Component({
  selector: 'app-section',
  imports: [CardComponent],
  templateUrl: './section.html',
  styles: [`h2{font-size:28px;margin:0 0 24px}p{color:var(--cor-texto-suave);margin:0}`],
})
export class SectionComponent { readonly data = toSignal(inject(ActivatedRoute).data); }

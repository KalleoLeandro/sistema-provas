import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonDirective } from '../../design-system/components';

@Component({
  selector: 'app-nao-encontrada',
  imports: [RouterLink, ButtonDirective],
  templateUrl: './nao-encontrada.html',
  styleUrl: './nao-encontrada.css',
})
export class NaoEncontradaComponent {}

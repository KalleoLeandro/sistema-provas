import { Component, inject } from '@angular/core';
import { CardComponent } from '../../design-system/components';
import { AuthService } from '../../services/auth.service';
import { DashboardIndicadoresComponent } from './indicadores';

@Component({
  selector: 'app-administrador-dashboard',
  imports: [CardComponent,DashboardIndicadoresComponent],
  templateUrl: './administrador-dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdministradorDashboardComponent { readonly auth = inject(AuthService); }

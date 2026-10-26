import { Component, inject } from '@angular/core';
import { AdministradorDashboardComponent } from './administrador-dashboard';
import { ProfessorDashboardComponent } from './professor-dashboard';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [AdministradorDashboardComponent, ProfessorDashboardComponent],
  templateUrl: './dashboard.html',
})
export class DashboardComponent { readonly auth = inject(AuthService); }

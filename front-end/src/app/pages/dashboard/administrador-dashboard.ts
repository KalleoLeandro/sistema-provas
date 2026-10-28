import { Component } from '@angular/core';
import { DailyThoughtComponent } from './daily-thought';
import { DashboardIndicadoresComponent } from './indicadores';

@Component({
  selector: 'app-administrador-dashboard',
  imports: [DailyThoughtComponent,DashboardIndicadoresComponent],
  templateUrl: './administrador-dashboard.html',
  styleUrl: './dashboard.css',
})
export class AdministradorDashboardComponent {}

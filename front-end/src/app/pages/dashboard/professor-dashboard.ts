import { DailyThoughtComponent } from './daily-thought';
import { Component } from '@angular/core';
import { DashboardIndicadoresComponent } from './indicadores';
@Component({selector:'app-professor-dashboard',imports:[DailyThoughtComponent,DashboardIndicadoresComponent],templateUrl:'./professor-dashboard.html',styleUrl:'./dashboard.css'})
export class ProfessorDashboardComponent {}

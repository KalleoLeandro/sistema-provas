import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { AlertComponent,ButtonDirective,CardComponent } from '../../design-system/components';
import { ApiService } from '../../services/api.service';
import { Professor } from '../../models/professor';
@Component({selector:'app-perfil-professor',imports:[AlertComponent,ButtonDirective,CardComponent],templateUrl:'./perfil.html',styleUrl:'../../design-system/profile.css'})
export class PerfilProfessorComponent implements OnInit,OnDestroy {
 private readonly api=inject(ApiService);private visit=0;
 readonly professor=signal<Professor|null>(null);readonly loading=signal(false);readonly erro=signal('');
 ngOnInit(){void this.load();}ngOnDestroy(){this.visit++;this.professor.set(null);}
 async load(){if(this.loading())return;const visit=++this.visit;this.loading.set(true);this.erro.set('');try{const p=await this.api.get<Professor>('/professor/dashboard');if(visit===this.visit)this.professor.set(p);}catch{if(visit===this.visit)this.erro.set('Não foi possível carregar o perfil. Tente novamente.');}finally{if(visit===this.visit)this.loading.set(false);}}
}

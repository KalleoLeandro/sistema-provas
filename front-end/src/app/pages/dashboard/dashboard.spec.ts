import {provideRouter} from '@angular/router';
import {signal} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {vi} from 'vitest';
import {AuthService} from '../../services/auth.service';
import {ApiService} from '../../services/api.service';
import {DashboardComponent} from './dashboard';
describe('Dashboard por perfil',()=>{
 const get=vi.fn();const usuario=signal({id:1,nome:'Usuário',login:'usuario',perfil:'administrador'});
 const indicadores={alunosFizeram:0,alunosPendentes:0,mediaGeral:null,naoIniciadas:0,emAndamento:0,aguardandoCorrecao:0,corrigidas:0,mediasTurmas:[]};
 beforeEach(()=>{get.mockReset().mockResolvedValue(indicadores);usuario.set({id:1,nome:'Usuário',login:'usuario',perfil:'administrador'});TestBed.configureTestingModule({providers:[provideRouter([]),{provide:AuthService,useValue:{usuario}},{provide:ApiService,useValue:{get}}]});});
 it('administrador vê três cards sem consultar dados de professor',async()=>{const f=TestBed.createComponent(DashboardComponent);await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).not.toContain('Carregando indicadores');});expect(f.nativeElement.querySelectorAll('.dashboard-metrics visual-metric-card')).toHaveLength(3);expect(get).not.toHaveBeenCalledWith('/professor/dashboard');});
 it('professor vê questões pendentes junto aos demais cards sem carregar matérias no dashboard',async()=>{usuario.set({id:2,nome:'Professor',login:'prof',perfil:'professor'});get.mockImplementation((p:string)=>Promise.resolve(p.endsWith('pendencias-questoes')?[{quantidades:[{faltantes:3}]}]:indicadores));const f=TestBed.createComponent(DashboardComponent);await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).not.toContain('Carregando indicadores');});expect(f.nativeElement.querySelectorAll('.dashboard-metrics visual-metric-card')).toHaveLength(4);expect(f.nativeElement.querySelector('.dashboard-metrics').textContent).toContain('Questões a cadastrar faltando');expect(f.nativeElement.textContent).not.toContain('Minhas matérias');expect(get).not.toHaveBeenCalledWith('/professor/dashboard');});
 it('falha nas questões não oculta indicadores nem apresenta zero fictício',async()=>{usuario.set({id:2,nome:'Professor',login:'prof',perfil:'professor'});get.mockImplementation((p:string)=>p.endsWith('pendencias-questoes')?Promise.reject(Error('Falha')):Promise.resolve(indicadores));const f=TestBed.createComponent(DashboardComponent);await f.whenStable();await vi.waitFor(()=>{f.detectChanges();expect(f.nativeElement.textContent).not.toContain('Carregando indicadores');});expect(f.nativeElement.querySelectorAll('.dashboard-metrics visual-metric-card')).toHaveLength(3);expect(f.nativeElement.textContent).toContain('Não foi possível verificar');});
});

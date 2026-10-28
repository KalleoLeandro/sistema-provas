import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { ChartComponent } from './chart';

describe('Gráficos de desempenho',()=>{
 beforeEach(()=>{vi.spyOn(HTMLCanvasElement.prototype,'getContext').mockReturnValue(null);vi.spyOn(console,'error').mockImplementation(()=>{});});
 afterEach(()=>vi.restoreAllMocks());
 it('usa cores distintas nas barras e preserva a ausência de média',()=>{
  const f=TestBed.createComponent(ChartComponent);f.componentRef.setInput('labels',Array.from({length:12},(_,i)=>'Turma '+i));f.componentRef.setInput('values',[7.5,null]);f.componentRef.setInput('maximum',10);f.detectChanges();
  const config=(f.componentInstance as any).chart.config;expect(new Set(config.data.datasets[0].backgroundColor).size).toBe(12);expect(config.data.datasets[0].data).toEqual([7.5,null]);expect(config.options.scales.y).toMatchObject({beginAtZero:true,max:10});expect(f.nativeElement.querySelector('details')).toBeNull();
 });
 it('renderiza donut com legenda das situações e sem eixo numérico',()=>{
  const f=TestBed.createComponent(ChartComponent);f.componentRef.setInput('type','doughnut');f.detectChanges();const config=(f.componentInstance as any).chart.config;expect(config.type).toBe('doughnut');expect(config.options.plugins.legend.display).toBe(true);expect(config.options.scales.y).toBeUndefined();
 });
});

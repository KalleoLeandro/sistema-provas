import { TestBed } from '@angular/core/testing';
import { InfoTooltipComponent } from './info-tooltip';
describe('Informação contextual da tela', () => {
  it('abre por foco ou toque, associa a descrição e fecha com Escape ou clique externo', async () => {
    const f=TestBed.createComponent(InfoTooltipComponent);
    f.componentRef.setInput('text','Orientação da tela'); await f.whenStable();
    const button=f.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(f.nativeElement.querySelector('[role=tooltip]')).toBeNull();
    button.dispatchEvent(new FocusEvent('focus')); await f.whenStable();
    const tooltip=f.nativeElement.querySelector('[role=tooltip]');
    expect(tooltip.textContent).toBe('Orientação da tela');
    expect(button.getAttribute('aria-describedby')).toBe(tooltip.id);
    button.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true})); await f.whenStable();
    expect(f.nativeElement.querySelector('[role=tooltip]')).toBeNull();
    button.click(); await f.whenStable();expect(f.nativeElement.querySelector('[role=tooltip]')).toBeTruthy();
    document.body.click(); await f.whenStable();expect(f.nativeElement.querySelector('[role=tooltip]')).toBeNull();
  });
});

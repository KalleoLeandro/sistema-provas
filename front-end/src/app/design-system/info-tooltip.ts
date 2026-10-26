import { Component, ElementRef, HostListener, Input, ViewChild, inject, signal } from '@angular/core';

@Component({ selector: 'visual-info-tooltip', templateUrl: './info-tooltip.html', styleUrl: './info-tooltip.css' })
export class InfoTooltipComponent {
  private static nextId = 0;
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  @ViewChild('trigger', {static:true}) trigger!: ElementRef<HTMLButtonElement>;
  @Input({required:true}) text = '';
  @Input() label = 'Informações sobre esta tela';
  readonly tooltipId = 'info-tooltip-' + ++InfoTooltipComponent.nextId;
  readonly visible = signal(false);
  left = 0; top = 0; width = 340; above = false;
  private focused = false;
  private hovered = false;

  show() {
    const rect = this.trigger.nativeElement.getBoundingClientRect();
    this.width = Math.min(340, window.innerWidth - 24);
    this.left = Math.max(12, Math.min(rect.left, window.innerWidth - this.width - 12));
    this.above = window.innerHeight - rect.bottom < 180 && rect.top > 180;
    this.top = this.above ? rect.top - 8 : rect.bottom + 8;
    this.visible.set(true);
  }
  focus() { this.focused=true; this.show(); }
  blur() { this.focused=false; if (!this.hovered) this.visible.set(false); }
  @HostListener('mouseenter') enter() { this.hovered=true; this.show(); }
  @HostListener('mouseleave') leave() { this.hovered=false; if (!this.focused) this.visible.set(false); }
  @HostListener('document:keydown.escape') close() { this.visible.set(false); }
  @HostListener('window:resize') resize() { if (this.visible()) this.show(); }
  @HostListener('window:scroll') scroll() { if (this.visible()) this.show(); }
  @HostListener('document:click', ['$event']) outside(event: MouseEvent) {
    if (!this.element.nativeElement.contains(event.target as Node)) this.close();
  }
}

import { Component, ElementRef, HostListener, Input, Output, EventEmitter, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { OverlayModule, ConnectedPosition } from '@angular/cdk/overlay';

export interface NavigationItem { id: string; label: string; route?: string; children?: NavigationItem[]; }

@Component({ selector: 'visual-navigation-menu', imports: [RouterLink, RouterLinkActive, OverlayModule],
  templateUrl: './navigation-menu.html', styleUrl: './navigation-menu.css',
})
export class NavigationMenuComponent {
  private static nextId=0;
  private readonly router=inject(Router);
  private readonly element=inject<ElementRef<HTMLElement>>(ElementRef);
  @Input() items: NavigationItem[]=[];
  @Input() mode: 'sidebar' | 'panel'='sidebar';
  @Input() idPrefix='navigation-'+ ++NavigationMenuComponent.nextId;
  @Output() navigate=new EventEmitter<void>();
  @Output() back=new EventEmitter<void>();
  readonly openId=signal<string|null>(null);
  readonly positions: ConnectedPosition[]=[
    {originX:'end',originY:'top',overlayX:'start',overlayY:'top',offsetX:8},
    {originX:'start',originY:'top',overlayX:'end',overlayY:'top',offsetX:-8},
    {originX:'start',originY:'bottom',overlayX:'start',overlayY:'top',offsetY:8},
  ];
  active(item: NavigationItem): boolean {
    return !!item.route && this.router.isActive(item.route,{paths:'exact',queryParams:'ignored',fragment:'ignored',matrixParams:'ignored'})
      || !!item.children?.some(child=>this.active(child));
  }
  toggle(item: NavigationItem) { this.openId.set(this.openId()===item.id?null:item.id); }
  close(restore=false) {
    const id=this.openId();this.openId.set(null);
    if (restore && id) document.getElementById(this.idPrefix+'-trigger-'+id)?.focus();
  }
  select() { this.close();this.navigate.emit(); }
  outside(event: MouseEvent) { if (!this.element.nativeElement.contains(event.target as Node)) this.close(); }
  keydown(event: KeyboardEvent,item: NavigationItem,index: number) {
    if(event.key==='ArrowRight' && item.children?.length) {
      event.preventDefault();this.openId.set(item.id);
      setTimeout(()=>document.getElementById(this.idPrefix+'-panel-'+item.id)?.querySelector<HTMLElement>('button,a')?.focus());
    } else if(event.key==='ArrowLeft' && this.mode==='panel') {
      event.preventDefault();this.back.emit();
    } else if(['ArrowDown','ArrowUp','Home','End'].includes(event.key)) {
      event.preventDefault();
      const rows=(event.currentTarget as HTMLElement).closest('.navigation-items')?.querySelectorAll<HTMLElement>(':scope > .navigation-entry > button, :scope > .navigation-entry > a');
      if (!rows?.length) return;
      const next=event.key==='Home'?0:event.key==='End'?rows.length-1:(index+(event.key==='ArrowDown'?1:rows.length-1))%rows.length;
      rows[next].focus();
    }
  }
  @HostListener('document:keydown.escape') escape() { this.close(true); }
  @HostListener('window:resize') resize() { this.close(); }
}

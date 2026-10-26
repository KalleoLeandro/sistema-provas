import { Component, Input, Output, EventEmitter } from '@angular/core';
export interface TabOption { id: string; label: string; }
@Component({ selector: 'visual-tabs', templateUrl: './tabs.html', styleUrl: './tabs.css' })
export class TabsComponent {
  @Input() tabs: TabOption[] = [];
  @Input() selected = '';
  @Input() idPrefix = 'abas';
  @Input() label = 'Abas';
  @Output() selectedChange = new EventEmitter<string>();
  keydown(event: KeyboardEvent, index: number) {
    let next = index;
    if (event.key === 'ArrowRight') next = (index + 1) % this.tabs.length;
    else if (event.key === 'ArrowLeft') next = (index + this.tabs.length - 1) % this.tabs.length;
    else if (event.key === 'Home') next = 0;
    else if (event.key === 'End') next = this.tabs.length - 1;
    else return;
    event.preventDefault(); this.selectedChange.emit(this.tabs[next].id);
    (event.currentTarget as HTMLElement).parentElement?.querySelectorAll<HTMLButtonElement>('[role=tab]')[next]?.focus();
  }
}

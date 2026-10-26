import { AfterContentInit, Component, ElementRef, HostListener, OnDestroy, inject, signal } from '@angular/core';

@Component({ selector:'visual-table-scroll', templateUrl:'./table-scroll.html', styleUrl:'./table-scroll.css' })
export class TableScrollComponent implements AfterContentInit, OnDestroy {
  private readonly element=inject<ElementRef<HTMLElement>>(ElementRef);
  private viewport:HTMLElement|null=null;
  private observer?:ResizeObserver;
  private destroyed=false;
  private readonly onScroll=()=>this.update();
  readonly maximum=signal(0);
  readonly position=signal(0);
  readonly thumbWidth=signal(40);

  ngAfterContentInit() {
    this.viewport=this.element.nativeElement.querySelector<HTMLElement>('.tabela-visual');
    if (!this.viewport) return;
    this.viewport.classList.add('com-controle-rolagem');
    this.viewport.addEventListener('scroll',this.onScroll,{passive:true});
    if(typeof ResizeObserver!=='undefined') {
      this.observer=new ResizeObserver(()=>this.update());
      this.observer.observe(this.viewport);
      const table=this.viewport.querySelector('table');if(table)this.observer.observe(table);
    }
    queueMicrotask(()=>this.update());
  }
  @HostListener('window:resize') update() {
    if(this.destroyed || !this.viewport)return;
    const width=this.viewport.clientWidth,total=this.viewport.scrollWidth;
    this.maximum.set(Math.max(0,total-width));
    this.position.set(Math.max(0,Math.min(this.maximum(),this.viewport.scrollLeft)));
    this.thumbWidth.set(Math.max(32,Math.min(120,width*(width/Math.max(1,total)))));
  }
  move(value:number) {
    if(!this.viewport)return;
    this.viewport.scrollLeft=Math.max(0,Math.min(this.maximum(),value));
    this.position.set(this.viewport.scrollLeft);
  }
  input(event:Event) { this.move(Number((event.target as HTMLInputElement).value)); }
  keydown(event:KeyboardEvent) {
    let target=this.position();
    if(event.key==='ArrowRight')target+=80;
    else if(event.key==='ArrowLeft')target-=80;
    else if(event.key==='Home')target=0;
    else if(event.key==='End')target=this.maximum();
    else if(event.key==='PageDown')target+=this.viewport?.clientWidth||80;
    else if(event.key==='PageUp')target-=this.viewport?.clientWidth||80;
    else return;
    event.preventDefault();this.move(target);
  }
  ngOnDestroy() {
    this.destroyed=true;this.observer?.disconnect();
    this.viewport?.removeEventListener('scroll',this.onScroll);
    this.viewport?.classList.remove('com-controle-rolagem');
  }
}

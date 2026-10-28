import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ListSort } from './list-sort';
@Component({selector:'th[sortColumn]',templateUrl:'./sort-header.html',styleUrl:'./sort-header.css',host:{'[attr.aria-sort]':'sort.column()===sortColumn?(sort.direction()==="ASC"?"ascending":"descending"):"none"'}})
export class SortHeaderComponent {
 @Input({required:true}) sortColumn='';@Input({required:true}) sort!:ListSort;
 @Output() sortChange=new EventEmitter<string>();
 toggle(){this.sort.toggle(this.sortColumn);this.sortChange.emit(this.sortColumn);}
}

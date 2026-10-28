import { signal } from '@angular/core';
export type SortValue = string|number|boolean|null|undefined|readonly (string|number|null|undefined)[];
const collator=new Intl.Collator('pt-BR',{sensitivity:'base'});
/** Ordena uma cópia da lista inteira antes da paginação, sem alterar os dados recebidos. */
export class ListSort {
 readonly column=signal('');readonly direction=signal<'ASC'|'DESC'>('ASC');
 toggle(column:string){this.direction.set(this.column()===column&&this.direction()==='ASC'?'DESC':'ASC');this.column.set(column);}
 reset(){this.column.set('');this.direction.set('ASC');}
 query(){return this.column()?'&ordenacao='+encodeURIComponent(this.column())+'&direcao='+this.direction():'';}
 ordered<T>(rows:readonly T[],value:(row:T,column:string)=>SortValue):T[]{
  const column=this.column(),direction=this.direction();if(!column)return [...rows];
  const compare=(a:SortValue,b:SortValue):number=>{
   if(a==null)return b==null?0:1;if(b==null)return -1;
   if(Array.isArray(a)&&Array.isArray(b)){for(let i=0;i<Math.max(a.length,b.length);i++){const n=compare(a[i],b[i]);if(n)return n;}return 0;}
   const n=typeof a==='number'&&typeof b==='number'?a-b:collator.compare(String(a),String(b));return direction==='ASC'?n:-n;
  };
  return [...rows].sort((a,b)=>compare(value(a,column),value(b,column))||compare((a as {id?:number}).id,(b as {id?:number}).id));
 }
}

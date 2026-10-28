import { type Meta,type StoryObj } from '@storybook/angular-vite';
import { SortHeaderComponent } from '../sort-header';
import { ListSort } from '../list-sort';
const meta={title:'Componentes/Tabela com ordenação',component:SortHeaderComponent} satisfies Meta<SortHeaderComponent>;
export default meta;
export const Padrao:StoryObj<SortHeaderComponent>={render:()=>({props:{sort:new ListSort(),rows:[{id:1,nome:'Beatriz',numero:10},{id:2,nome:'Ana',numero:2},{id:3,nome:'Carlos',numero:1}],value:(r:{nome:string;numero:number},c:string)=>c==='numero'?r.numero:r.nome},template:'<div class="tabela-visual"><table><thead><tr><th sortColumn="nome" [sort]="sort">Nome</th><th sortColumn="numero" [sort]="sort">Número</th></tr></thead><tbody>@for(row of sort.ordered(rows,value);track row.id){<tr><td>{{row.nome}}</td><td>{{row.numero}}</td></tr>}</tbody></table></div>'})};

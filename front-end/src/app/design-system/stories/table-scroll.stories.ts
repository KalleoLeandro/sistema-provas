import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { TableScrollComponent } from '../table-scroll';
const meta={title:'Componentes/Tabela com rolagem persistente',component:TableScrollComponent} satisfies Meta<TableScrollComponent>;
export default meta;
export const Padrao:StoryObj<TableScrollComponent>={render:()=>({props:{rows:Array.from({length:30},(_,i)=>i+1)},template:'<visual-table-scroll><div class="tabela-visual tabela-rolagem"><table><thead><tr><th>Nome</th><th>Matéria</th><th>Turma</th><th>Informações adicionais</th></tr></thead><tbody>@for(row of rows;track row){<tr><td>Registro {{row}}</td><td>Matemática</td><td>Fundamental — 1º ano A</td><td>Exemplo de conteúdo para testar a rolagem horizontal</td></tr>}</tbody></table></div></visual-table-scroll>'})};

import { applicationConfig, type Meta, type StoryObj } from '@storybook/angular-vite';
import { provideRouter } from '@angular/router';
import { NavigationMenuComponent } from '../navigation-menu';
const meta={id:'componentes-sidebar-menu',title:'Componentes/Menu lateral',component:NavigationMenuComponent,
 decorators:[applicationConfig({providers:[provideRouter([])]})],
 args:{items:[{id:'cadastros',label:'Cadastros',children:[{id:'usuarios',label:'Usuários',children:[{id:'cadastrar',label:'Cadastrar',route:'/cadastro/usuarios/cadastrar'},{id:'listar',label:'Listar',route:'/cadastro/usuarios/listar'}]},{id:'materias',label:'Matérias',children:[{id:'listar',label:'Listar',route:'/cadastro/materias/listar'}]}]}]},
} satisfies Meta<NavigationMenuComponent>;
export default meta;
export const Default:StoryObj<NavigationMenuComponent>={name:'Submenus laterais',render:args=>({props:args,template:'<div style="width:264px;padding:20px;background:var(--cor-sidebar)"><visual-navigation-menu [items]="items" /></div>'})};

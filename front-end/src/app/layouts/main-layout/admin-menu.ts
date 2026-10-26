import { Component, output } from '@angular/core';
import { NavigationMenuComponent, NavigationItem } from '../../design-system/navigation-menu';
@Component({ selector:'app-admin-menu', imports:[NavigationMenuComponent], templateUrl:'./admin-menu.html' })
export class AdminMenuComponent {
  readonly navigate=output<void>();
  readonly items: NavigationItem[]=[
    {id:'cadastros',label:'Cadastros',children:[
      ...[{id:'usuarios',label:'Usuários'},{id:'materias',label:'Matérias'},{id:'turmas',label:'Turmas'}].map(item=>({...item,children:[
        {id:'cadastrar',label:'Cadastrar',route:'/cadastro/'+item.id+'/cadastrar'},
        {id:'listar',label:'Listar',route:'/cadastro/'+item.id+'/listar'},
      ]})),
    ]},
    {id:'relatorios',label:'Relatórios',route:'/relatorios'},
  ];
}

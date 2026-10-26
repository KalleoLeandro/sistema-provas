import { Component, output } from '@angular/core';
import { NavigationMenuComponent, NavigationItem } from '../../design-system/navigation-menu';
@Component({ selector:'app-professor-menu', imports:[NavigationMenuComponent], templateUrl:'./professor-menu.html' })
export class ProfessorMenuComponent {
  readonly navigate=output<void>();
  readonly items: NavigationItem[]=[
    {id:'questoes',label:'Questões',children:[
      {id:'cadastrar',label:'Cadastrar',route:'/questoes/cadastrar'},
      {id:'listar',label:'Listar',route:'/questoes/listar'},
    ]},
    {id:'provas',label:'Provas',route:'/provas'},
    {id:'perfil',label:'Meu perfil',route:'/perfil'},
    {id:'relatorios',label:'Relatórios',route:'/relatorios'},
    {id:'alunos',label:'Alunos',children:[{id:'listar',label:'Listar',route:'/alunos/listar'}]},
  ];
}

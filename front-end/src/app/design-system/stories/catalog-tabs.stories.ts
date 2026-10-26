import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { TabsComponent } from '../tabs';
const meta = { id: 'componentes-tabs', title: 'Componentes/Abas', component: TabsComponent,
  args: { selected: 'cadastradas', idPrefix: 'exemplo', tabs: [{id:'cadastradas',label:'Questões cadastradas'},{id:'faltantes',label:'Questões que faltam cadastrar'}] },
} satisfies Meta<TabsComponent>;
export default meta;
export const Default: StoryObj<TabsComponent> = { name: 'Padrão', render: args => ({ props: args,
  template: '<visual-tabs [tabs]="tabs" [(selected)]="selected" [idPrefix]="idPrefix" /><section role="tabpanel" [id]="idPrefix + \'-panel-\' + selected" [attr.aria-labelledby]="idPrefix + \'-tab-\' + selected"><p>{{ selected === \'cadastradas\' ? \'Lista de questões cadastradas\' : \'Questões pendentes de cadastro\' }}</p></section>',
}) };

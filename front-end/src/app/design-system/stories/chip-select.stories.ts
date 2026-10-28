import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ChipSelectComponent } from '../chip-select';

export default {
  title: 'Design system/Seleção com chips', component: ChipSelectComponent,
  decorators: [moduleMetadata({ imports: [FormsModule] })], tags: ['autodocs'],
} satisfies Meta<ChipSelectComponent>;

export const Padrao: StoryObj<ChipSelectComponent> = {
  name: 'Padrão',
  render: () => ({
    props: { selecionadas: [1], opcoes: [{ value: 1, label: 'Matemática' }, { value: 2, label: 'Português' }, { value: 3, label: 'História' }] },
    template: '<visual-chip-select inputId="exemplo-materias" label="Matérias" placeholder="Digite o nome da matéria" [options]="opcoes" [(ngModel)]="selecionadas" />',
  }),
};

export const AssuntoUnico:StoryObj<ChipSelectComponent>={name:'Assunto com sugestões e criação',render:()=>{
 const state={selecionadas:[] as number[],opcoes:[{value:1,label:'Frações'},{value:2,label:'Equações'}],criar(nome:string){const option={value:this.opcoes.length+1,label:nome};this.opcoes=[...this.opcoes,option];this.selecionadas=[option.value];}};
 return {props:state,template:'<visual-chip-select inputId="assunto-exemplo" label="Assunto" placeholder="Digite um assunto" [options]="opcoes" [(ngModel)]="selecionadas" [maxSelected]="1" [allowCreate]="true" [fuzzySearch]="true" createLabel="Criar assunto" (createOption)="criar($event)" />'};}};

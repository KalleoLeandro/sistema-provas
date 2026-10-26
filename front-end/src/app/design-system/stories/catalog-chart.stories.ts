import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { ChartComponent } from '../library';
const meta = {
  id: "componentes-chart", title: "Componentes/Gráfico",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, ChartComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({ props: { ...{} }, template: '<visual-chart />' }),
};
export const MediasPorTurma: StoryObj = {
  name:'Médias por turma',
  render:()=>({props:{labels:['Fundamental — 1º ano A','Fundamental — 1º ano B','Médio — 1º ano A'],values:[7.5,8.2,null]},template:'<visual-chart label="Média de notas (0 a 10)" [labels]="labels" [values]="values" type="bar" [maximum]="10" />'}),
};
export const SituacaoDasProvas: StoryObj = {
  name:'Situação das provas (donut)',
  render:()=>({props:{labels:['Não iniciadas','Em andamento','Aguardando correção','Corrigidas'],values:[12,3,5,20]},template:'<visual-chart label="Avaliações por situação" [labels]="labels" [values]="values" type="doughnut" />'}),
};

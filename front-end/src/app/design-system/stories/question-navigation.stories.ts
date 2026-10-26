import { type Meta,type StoryObj } from '@storybook/angular-vite';
import { QuestionNavigationComponent } from '../question-navigation';
export default {title:'Componentes/Navegação de questões',component:QuestionNavigationComponent} satisfies Meta<QuestionNavigationComponent>;
export const Padrao:StoryObj<QuestionNavigationComponent>={args:{total:10,answered:[1,3,4]}};

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

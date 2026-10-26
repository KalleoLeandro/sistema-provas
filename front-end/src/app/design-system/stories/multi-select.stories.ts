import { Meta, StoryObj, moduleMetadata } from '@storybook/angular-vite';
import { FormsModule } from '@angular/forms';
import { MultiSelectComponent } from '../multi-select';

export default {
  title: 'Design system/Seleção múltipla',
  component: MultiSelectComponent,
  decorators: [moduleMetadata({ imports: [FormsModule] })],
  tags: ['autodocs'],
} satisfies Meta<MultiSelectComponent>;

export const Padrao: StoryObj<MultiSelectComponent> = {
  name: 'Padrão',
  render: () => ({
    props: {
      selecionadas: [1],
      opcoes: [
        { value: 1, label: 'Fundamental — 1º ano A' },
        { value: 2, label: 'Fundamental — 2º ano A' },
        { value: 3, label: 'Médio — 1º ano A' },
      ],
    },
    template: '<visual-multi-select label="Turmas" [options]="opcoes" [(ngModel)]="selecionadas" /><p>Selecionadas: {{ selecionadas.join(", ") }}</p>',
  }),
};

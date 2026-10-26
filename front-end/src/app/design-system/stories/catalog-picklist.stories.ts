import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTransferModule } from '../library';
const meta = {
  id: "componentes-picklist", title: "Componentes/Lista de transferência",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTransferModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{
        items: [
          { key: '1', title: 'Português' },
          { key: '2', title: 'Matemática' },
          { key: '3', title: 'História' },
        ],
      },
    },
    template:
      '<nz-transfer [nzDataSource]="items" [nzTitles]="[\'Disponíveis\', \'Selecionados\']" nzShowSearch  />',
  }),
};

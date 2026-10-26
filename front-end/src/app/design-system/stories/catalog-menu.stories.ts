import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzDropdownModule, NzMenuModule } from '../library';
const meta = {
  title: "Componentes/Menu",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [
        FormsModule,
        ButtonDirective,
        InputDirective,
        FieldComponent,
        NzDropdownModule,
        NzMenuModule,
      ],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ action: '' } },
    template:
      '<button botaoVisual nz-dropdown [nzDropdownMenu]="menu" nzTrigger="click">Ações</button><nz-dropdown-menu #menu="nzDropdownMenu"><ul nz-menu><li nz-menu-item (click)="action = \'Editar\'">Editar</li><li nz-menu-item (click)="action = \'Listar\'">Listar</li></ul></nz-dropdown-menu><p role="status">{{action}}</p>',
  }),
};

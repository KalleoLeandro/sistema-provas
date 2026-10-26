import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzRadioModule } from '../library';
const meta = {
  id: "componentes-selection-buttons", title: "Componentes/Botões de seleção",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzRadioModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ profile: 'administrador' } },
    template:
      '<nz-radio-group [(ngModel)]="profile" nzButtonStyle="solid"><label nz-radio-button nzValue="administrador">Administrador</label><label nz-radio-button nzValue="professor">Professor</label><label nz-radio-button nzValue="aluno">Aluno</label></nz-radio-group>',
  }),
};

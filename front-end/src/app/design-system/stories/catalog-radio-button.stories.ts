import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzRadioModule } from '../library';
const meta = {
  id: "componentes-radio-button", title: "Componentes/Botão de opção",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
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
      '<nz-radio-group [(ngModel)]="profile"><label nz-radio nzValue="administrador">Administrador</label><label nz-radio nzValue="professor">Professor</label><label nz-radio nzValue="aluno">Aluno</label></nz-radio-group><p>Perfil: {{profile}}</p>',
  }),
};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSelectModule } from '../library';
const meta = {
  id: "componentes-select-field", title: "Componentes/Campo de seleção",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSelectModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ profile: 'administrador' } },
    template:
      '<label class="ds-stack">Perfil<nz-select class="ds-full" [(ngModel)]="profile" nzPlaceHolder="Selecione"><nz-option nzValue="administrador" nzLabel="Administrador" /><nz-option nzValue="professor" nzLabel="Professor" /><nz-option nzValue="aluno" nzLabel="Aluno" /></nz-select></label>',
  }),
};

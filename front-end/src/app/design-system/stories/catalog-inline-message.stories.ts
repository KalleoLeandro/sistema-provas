import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzAlertModule } from '../library';
const meta = {
  id: "componentes-inline-message", title: "Componentes/Mensagem em linha",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzAlertModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<div class="ds-stack"><nz-alert nzType="success" nzMessage="Cadastro realizado." nzShowIcon /><nz-alert nzType="info" nzMessage="Selecione um perfil." nzShowIcon /><nz-alert nzType="warning" nzMessage="Revise os dados antes de continuar." nzShowIcon /><nz-alert nzType="error" nzMessage="Não foi possível cadastrar." nzShowIcon /></div>',
  }),
};

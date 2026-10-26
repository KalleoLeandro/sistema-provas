import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzBreadCrumbModule } from '../library';
const meta = {
  id: "componentes-breadcrumbs", title: "Componentes/Trilha de navegação",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzBreadCrumbModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<nz-breadcrumb><nz-breadcrumb-item><a href="#inicio">Início</a></nz-breadcrumb-item><nz-breadcrumb-item><a href="#cadastros">Cadastros</a></nz-breadcrumb-item><nz-breadcrumb-item>Usuários</nz-breadcrumb-item></nz-breadcrumb>',
  }),
};

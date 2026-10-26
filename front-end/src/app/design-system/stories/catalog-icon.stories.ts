import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzIconModule } from '../library';
const meta = {
  id: "componentes-icon", title: "Componentes/Ícone",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzIconModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<div class="ds-row"><nz-icon class="ds-icon" nzType="home" aria-label="Início" /><nz-icon class="ds-icon" nzType="user" aria-label="Usuário" /><nz-icon class="ds-icon" nzType="bell" aria-label="Notificação" /></div>',
  }),
};

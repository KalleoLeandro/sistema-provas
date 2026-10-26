import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTagModule } from '../library';
const meta = {
  id: "componentes-tag", title: "Componentes/Etiqueta",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTagModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<div class="ds-row"><nz-tag nzColor="success">Ativo</nz-tag><nz-tag nzColor="default">Inativo</nz-tag><nz-tag nzColor="processing">Em andamento</nz-tag></div>',
  }),
};

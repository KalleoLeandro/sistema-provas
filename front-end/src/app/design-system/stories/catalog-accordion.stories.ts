import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzCollapseModule } from '../library';
const meta = {
  id: "componentes-accordion", title: "Componentes/Acordeão",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzCollapseModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<nz-collapse><nz-collapse-panel nzHeader="Dados do aluno" [nzActive]="true"><p>Informações do cadastro.</p></nz-collapse-panel><nz-collapse-panel nzHeader="Dados da turma"><p>Turma e período letivo.</p></nz-collapse-panel></nz-collapse>',
  }),
};

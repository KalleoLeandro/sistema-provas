import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzCheckboxModule } from '../library';
const meta = {
  id: "componentes-checkbox", title: "Componentes/Caixa de seleção",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzCheckboxModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ checked: true } },
    template:
      '<div class="ds-stack"><label nz-checkbox [(ngModel)]="checked">Perfil ativo</label><label nz-checkbox [nzDisabled]="true">Opção indisponível</label><p>Ativo: {{checked ? "Sim" : "Não"}}</p></div>',
  }),
};

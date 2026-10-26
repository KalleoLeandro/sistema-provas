import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSpinModule } from '../library';
const meta = {
  id: "componentes-spinner", title: "Componentes/Indicador de carregamento",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSpinModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<nz-spin nzTip="Carregando…" [nzSpinning]="true"><div class="ds-panel">Aguarde o carregamento dos dados.</div></nz-spin>',
  }),
};

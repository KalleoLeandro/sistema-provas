import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSwitchModule } from '../library';
const meta = {
  id: "componentes-switch-button", title: "Componentes/Interruptor",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSwitchModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ active: true } },
    template:
      '<div class="ds-row"><label for="switch-profile">Perfil ativo</label><nz-switch nzId="switch-profile" [(ngModel)]="active" nzCheckedChildren="Sim" nzUnCheckedChildren="Não" /></div>',
  }),
};

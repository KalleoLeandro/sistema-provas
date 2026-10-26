import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzDrawerModule } from '../library';
const meta = {
  id: "componentes-sheet", title: "Componentes/Painel lateral",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzDrawerModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ visible: false } },
    template:
      '<button botaoVisual (click)="visible = true">Abrir painel lateral</button><nz-drawer [nzVisible]="visible" nzTitle="Detalhes do cadastro" nzPlacement="right" (nzOnClose)="visible = false"><ng-container *nzDrawerContent><p>Dados e ações complementares do registro.</p></ng-container></nz-drawer>',
  }),
};

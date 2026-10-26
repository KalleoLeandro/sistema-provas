import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzModalModule } from '../library';
const meta = {
  id: "componentes-modal", title: "Componentes/Janela modal",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzModalModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ visible: false } },
    template:
      '<button botaoVisual (click)="visible = true">Abrir modal</button><nz-modal [(nzVisible)]="visible" nzTitle="Confirmar ação" (nzOnCancel)="visible = false" (nzOnOk)="visible = false"><ng-container *nzModalContent><p>Deseja continuar com esta ação?</p></ng-container></nz-modal>',
  }),
};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzStepsModule } from '../library';
const meta = {
  id: "componentes-stepper", title: "Componentes/Etapas",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzStepsModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ step: 0 } },
    template:
      '<div class="ds-stack"><nz-steps [nzCurrent]="step"><nz-step nzTitle="Perfil" /><nz-step nzTitle="Dados" /><nz-step nzTitle="Confirmação" /></nz-steps><p>Etapa {{step + 1}} de 3</p><div class="ds-row"><button botaoVisual variant="secondary" [disabled]="step === 0" (click)="step = step - 1">Voltar</button><button botaoVisual [disabled]="step === 2" (click)="step = step + 1">Próximo</button></div></div>',
  }),
};

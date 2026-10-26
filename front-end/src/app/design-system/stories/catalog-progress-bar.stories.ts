import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { ProgressComponent } from '../progress';
const meta = {
  id: "componentes-progress-bar", title: "Componentes/Barra de progresso",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, ProgressComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ progress: 40 } },
    template:
      '<div class="ds-stack"><visual-progress [total]="100" [completed]="progress" label="Progresso" /><div class="ds-row"><button botaoVisual variant="secondary" (click)="progress = progress > 0 ? progress - 10 : 0">Diminuir</button><button botaoVisual (click)="progress = progress < 100 ? progress + 10 : 100">Avançar</button></div></div>',
  }),
};

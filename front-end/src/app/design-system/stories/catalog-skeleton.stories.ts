import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSkeletonModule } from '../library';
const meta = {
  id: "componentes-skeleton", title: "Componentes/Esqueleto de carregamento",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSkeletonModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template: '<nz-skeleton [nzActive]="true" [nzAvatar]="true" />',
  }),
};

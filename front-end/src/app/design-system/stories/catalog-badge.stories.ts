import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzBadgeModule, NzAvatarModule } from '../library';
const meta = {
  id: "componentes-badge", title: "Componentes/Indicador",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [
        FormsModule,
        ButtonDirective,
        InputDirective,
        FieldComponent,
        NzBadgeModule,
        NzAvatarModule,
      ],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template: '<nz-badge [nzCount]="5"><nz-avatar nzText="AD" nzSize="large" /></nz-badge>',
  }),
};

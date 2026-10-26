import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  id: "componentes-illustration", title: "Componentes/Ilustração",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({ imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template:
      '<figure class="ds-figure"><img src="branding/esfinora-logo-blue.svg" width="360" alt="ESFINORA: esfinge e conhecimento" /><figcaption>Identidade visual do ESFINORA</figcaption></figure>',
  }),
};

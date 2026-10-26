import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { TagFieldComponent } from '../library';
const meta = {
  id: "componentes-tag-field", title: "Componentes/Campo de etiquetas",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, TagFieldComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ tags: ['Português', 'Matemática'] } },
    template: '<visual-tag-field [(ngModel)]="tags" /><p>Marcadores: {{tags.join(", ")}}</p>',
  }),
};

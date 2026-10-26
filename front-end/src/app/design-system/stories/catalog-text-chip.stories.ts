import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTagModule } from '../library';
const meta = {
  id: "componentes-text-chip", title: "Componentes/Etiqueta removível",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTagModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{ tags: ['Administrador', 'Professor', 'Aluno'] },
      keepTag: (removed: string) => (tag: string) => tag !== removed,
    },
    template:
      '<div class="ds-row">@for (tag of tags; track tag) {<nz-tag nzMode="closeable" (nzOnClose)="tags = tags.filter(keepTag(tag))">{{tag}}</nz-tag>}</div>',
  }),
};

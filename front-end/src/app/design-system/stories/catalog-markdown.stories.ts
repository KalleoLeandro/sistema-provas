import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { MarkdownComponent } from '../library';
const meta = {
  title: "Componentes/Markdown",
  parameters: {
    docs: {
      description: {
        component:
          'O conteúdo é convertido por marked e sanitizado pelo Angular; HTML inseguro não é liberado.',
      },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, MarkdownComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{
        source:
          '# Conteúdo da avaliação\n\nTexto com **destaque**, *ênfase* e lista:\n\n- Português\n- Matemática',
      },
    },
    template: '<visual-markdown [source]="source" />',
  }),
};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { ContentLineComponent } from '../library';
const meta = {
  id: "componentes-content-line", title: "Componentes/Linha de conteúdo",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, ContentLineComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-content-line label="Matéria" value="Matemática"><button botaoVisual variant="edit" size="compact" (click)="message = \'Edição solicitada\'">Editar</button></visual-content-line><p role="status">{{message}}</p>',
  }),
};

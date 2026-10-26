import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { FileUploadComponent } from '../library';
const meta = {
  id: "componentes-file-upload", title: "Componentes/Envio de arquivos",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, FileUploadComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-file-upload /><p class="ds-muted">Seleção local. Nenhum arquivo é enviado automaticamente.</p>',
  }),
};

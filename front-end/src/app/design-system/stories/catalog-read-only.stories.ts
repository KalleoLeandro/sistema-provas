import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { ReadOnlyComponent } from '../library';
const meta = {
  id: "componentes-read-only", title: "Componentes/Somente leitura",
  decorators: [
    moduleMetadata({ imports: [FormsModule, InputDirective, ButtonDirective, ReadOnlyComponent] }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<div class="ds-stack"><visual-read-only label="Perfil" value="Administrador" /><visual-read-only label="Status" value="Ativo" /></div>',
  }),
};

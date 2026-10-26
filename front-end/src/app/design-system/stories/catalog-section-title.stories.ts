import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { InputDirective, ButtonDirective } from '../components';
import { SectionTitleComponent } from '../library';
const meta = {
  id: "componentes-section-title", title: "Componentes/Título de seção",
  decorators: [
    moduleMetadata({
      imports: [FormsModule, InputDirective, ButtonDirective, SectionTitleComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { message: '' },
    template:
      '<visual-section-title eyebrow="CADASTRO" title="Usuários" description="Cadastre e gerencie os perfis de acesso." />',
  }),
};

import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
const meta = {
  id: "componentes-search-field", title: "Componentes/Campo de pesquisa",
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
    props: { ...{ query: '' } },
    template:
      '<label class="ds-stack">Pesquisar<input campoVisual type="search" [(ngModel)]="query" placeholder="Buscar por nome" /></label><p role="status">Busca: {{query || "Nenhuma"}}</p>',
  }),
};

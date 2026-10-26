import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzSelectModule } from '../library';
const meta = {
  id: "componentes-filter", title: "Componentes/Filtro",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzSelectModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ query: '', items: ['Português', 'Matemática', 'História'] } },
    template:
      '<div class="ds-stack"><label>Filtrar matérias<input campoVisual aria-label="Filtrar matérias" [(ngModel)]="query" type="search" /></label><ul>@for (item of items; track item) {@if (item.toLowerCase().includes(query.toLowerCase())) {<li>{{item}}</li>}}</ul></div>',
  }),
};

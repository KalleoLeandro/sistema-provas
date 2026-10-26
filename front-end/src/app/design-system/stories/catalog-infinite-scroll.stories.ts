import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { InfiniteScrollComponent } from '../library';
const meta = {
  id: "componentes-infinite-scroll", title: "Componentes/Rolagem infinita",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [
        FormsModule,
        ButtonDirective,
        InputDirective,
        FieldComponent,
        InfiniteScrollComponent,
      ],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{
        count: 5,
        entries: [
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
          26, 27, 28, 29, 30,
        ],
      },
    },
    template:
      '<visual-infinite-scroll [hasMore]="count < 30" (loadMore)="count = count + 5"><ul>@for (item of entries.slice(0, count); track item) {<li class="ds-panel">Registro {{item}}</li>}</ul></visual-infinite-scroll>',
  }),
};

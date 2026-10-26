import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { PaginationComponent } from '../pagination';
const meta = { id: 'componentes-paginator', title: 'Componentes/Paginação', component: PaginationComponent,
  args: { total: 120, page: 1, pageSize: 15 },
} satisfies Meta<PaginationComponent>;
export default meta;
export const Default: StoryObj<PaginationComponent> = {
  name: 'Padrão', render: args => ({ props: args, template: '<visual-pagination [total]="total" [(page)]="page" [(pageSize)]="pageSize" (pageSizeChange)="page=1" />' }),
};

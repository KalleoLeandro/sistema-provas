import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ConfirmDialogComponent } from '../confirm-dialog';
import { ButtonDirective } from '../components';
const meta = {
  title: 'Design system/Confirmação de exclusão',
  decorators: [moduleMetadata({ imports: [ConfirmDialogComponent, ButtonDirective] })],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { open: false, message: '' },
    template: `<button botaoVisual variant="danger" (click)="open=true">Excluir matéria</button><visual-confirm-dialog [(open)]="open" title="Excluir matéria" message="Deseja excluir a matéria Matemática? Esta ação não pode ser desfeita." (confirmed)="open=false;message='Matéria excluída no exemplo.'" /><p role="status">{{message}}</p>`,
  }),
};

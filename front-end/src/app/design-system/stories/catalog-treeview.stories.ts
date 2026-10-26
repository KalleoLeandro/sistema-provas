import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzTreeModule } from '../library';
const meta = {
  id: "componentes-treeview", title: "Componentes/Árvore de navegação",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTreeModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{
        nodes: [
          {
            title: 'Cadastros',
            key: 'cadastros',
            children: [
              {
                title: 'Usuários',
                key: 'usuarios',
                children: [
                  { title: 'Administradores', key: 'administradores', isLeaf: true },
                  { title: 'Professores', key: 'professores', isLeaf: true },
                  { title: 'Alunos', key: 'alunos', isLeaf: true },
                ],
              },
              { title: 'Matérias', key: 'materias', isLeaf: true },
              { title: 'Turmas', key: 'turmas', isLeaf: true },
            ],
          },
        ],
      },
    },
    template: '<nz-tree [nzData]="nodes" nzCheckable [nzExpandAll]="true" />',
  }),
};

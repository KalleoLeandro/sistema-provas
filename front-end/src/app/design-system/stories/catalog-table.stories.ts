import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent, RecordNameDirective } from '../components';
import { NzTableModule } from '../library';
const meta = {
  id: "componentes-table", title: "Componentes/Tabela",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFÍNORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzTableModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: {
      ...{
        rows: [
          { id: 1, nome: 'Administrador de exemplo', login: 'admin.exemplo', profile: 'Administrador', active: true },
          { id: 2, nome: 'Professor de exemplo', login: 'professor.exemplo', profile: 'Professor', active: true },
          { id: 3, nome: 'Aluno de exemplo', login: 'aluno.exemplo', profile: 'Aluno', active: false },
        ],
      },
    },
    template:
      '<nz-table #table [nzData]="rows" [nzPageSize]="2"><thead><tr><th scope="col">Nome</th><th>Login</th><th>Perfil</th><th>Status</th></tr></thead><tbody>@for (row of table.data; track row.id) {<tr><th nomeRegistro scope="row">{{row.nome}}</th><td>{{row.login}}</td><td>{{row.profile}}</td><td>{{row.active ? "Ativo" : "Inativo"}}</td></tr>}</tbody></nz-table>',
  }),
};

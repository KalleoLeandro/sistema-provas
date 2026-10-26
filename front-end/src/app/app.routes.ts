import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';
import { masterGuard } from './guards/master.guard';
export const rotas: Routes = [
    { path: 'indisponivel', loadComponent: () => import('./pages/indisponivel/indisponivel').then(modulo => modulo.IndisponivelComponent) },
    { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./pages/login/login').then(modulo => modulo.LoginComponent) },
    { path: 'design-system', canActivate: [authGuard, masterGuard], data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/design-system/design-system').then(modulo => modulo.DesignSystemComponent) },
    { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    {
        path: '',
        canActivateChild: [authGuard],
        loadComponent: () => import('./layouts/main-layout/main-layout').then(modulo => modulo.MainLayoutComponent),
        children: [
            { path: 'cadastro/administradores/cadastrar', pathMatch: 'full', redirectTo: 'cadastro/usuarios/cadastrar' },
            { path: 'cadastro/administradores/listar', pathMatch: 'full', redirectTo: 'cadastro/usuarios/listar' },
            { path: 'dashboard', data: { perfis: ['administrador', 'professor'] }, loadComponent: () => import('./pages/dashboard/dashboard').then(modulo => modulo.DashboardComponent) },
            { path: 'questoes/cadastrar', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/questoes/cadastrar/cadastrar').then(modulo => modulo.CadastrarQuestoesComponent) },
            { path: 'questoes/:id/editar', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/questoes/cadastrar/cadastrar').then(modulo => modulo.CadastrarQuestoesComponent) },
            { path: 'provas/correcoes/:id', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/provas/correcoes/corrigir').then(modulo => modulo.CorrigirProvaComponent) },
            { path: 'perfil', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/perfil/perfil').then(m => m.PerfilProfessorComponent) },
            { path: 'provas', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/provas/provas').then(modulo => modulo.ProvasComponent) },
            { path: 'alunos/listar', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/alunos/listar/listar').then(modulo => modulo.ListarAlunosComponent) },
            { path: 'questoes/listar', data: { perfis: ['professor'] }, loadComponent: () => import('./pages/questoes/listar/listar').then(modulo => modulo.ListarQuestoesComponent) },
            { path: 'cadastro/usuarios/:id/editar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/usuarios/editar/editar').then(modulo => modulo.EditarUsuarioComponent) },
            { path: 'cadastro/usuarios/listar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/usuarios/listar/listar').then(modulo => modulo.ListarUsuariosComponent) },
            { path: 'cadastro/materias/cadastrar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/materias/cadastrar/cadastrar').then(modulo => modulo.CadastrarMateriaComponent) },
            { path: 'cadastro/usuarios/cadastrar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/usuarios/cadastrar/cadastrar').then(modulo => modulo.CadastrarUsuarioComponent) },
            { path: 'cadastro/materias/:id/editar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/materias/editar/editar').then(modulo => modulo.EditarMateriaComponent) },
            { path: 'cadastro/materias/listar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/materias/listar/listar').then(modulo => modulo.ListarMateriasComponent) },
            { path: 'cadastro/turmas/cadastrar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/turmas/cadastrar/cadastrar').then(modulo => modulo.CadastrarTurmaComponent) },
            { path: 'cadastro/turmas/listar', data: { perfis: ['administrador'] }, loadComponent: () => import('./pages/turmas/listar/listar').then(modulo => modulo.ListarTurmasComponent) },
            { path: 'relatorios', data: { perfis: ['administrador', 'professor'] }, loadComponent: () => import('./pages/relatorios/relatorios').then(m => m.RelatoriosComponent) },
            { path: 'resultados-provas', pathMatch: 'full', redirectTo: 'relatorios' },
            { path: 'inicio', pathMatch: 'full', redirectTo: 'aluno/provas' },
            { path: 'aluno/provas/:id', data: { perfis: ['aluno'] }, canDeactivate: [(component: import('./pages/aluno/execucao/execucao').ExecucaoProvaComponent) => component.tryLeave()], loadComponent: () => import('./pages/aluno/execucao/execucao').then(modulo => modulo.ExecucaoProvaComponent) },
            { path: 'aluno/provas', data: { perfis: ['aluno'] }, loadComponent: () => import('./pages/aluno/provas/provas').then(modulo => modulo.ProvasAlunoComponent) }
        ]
    },
    { path: '**', title: 'Página não encontrada | ESFINORA', loadComponent: () => import('./pages/nao-encontrada/nao-encontrada').then(modulo => modulo.NaoEncontradaComponent) }
];

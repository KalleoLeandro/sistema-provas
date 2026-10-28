import { ListSort } from '../../../design-system/list-sort';
import { SortHeaderComponent } from '../../../design-system/sort-header';
import { Turma } from '../../../models/turma';
import { statusAluno } from '../../../models/status-aluno';
import { PaginationComponent } from '../../../design-system/pagination';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { Aluno, Pagina } from '../../../models/aluno';
import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Professor } from '../../../models/professor';
import { ApiService } from '../../../services/api.service';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent, CardComponent, RecordNameDirective, ButtonDirective, InputDirective, FieldComponent } from '../../../design-system/components';

interface UsuarioAdministrativo { id: number; nome: string; login: string; ativo: boolean; }

@Component({
  selector: 'app-listar-usuarios',
  imports: [SortHeaderComponent,FieldComponent, PaginationComponent, TableScrollComponent, RouterLink, AlertComponent, CardComponent, RecordNameDirective, ButtonDirective, InputDirective, FormsModule],
  templateUrl: './listar.html',
  styleUrls: ['./listar.css','../../../design-system/list-filters.css'],
})
export class ListarUsuariosComponent implements OnInit {
 readonly sort=new ListSort();
 changeSort(){if(this.perfil()==='aluno')void this.changeProfile('aluno');}

 readonly statusAluno=statusAluno;
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private requestId = 0;
  readonly busca=signal('');readonly filtroStatus=signal('');readonly filtroTurma=signal<number|null>(null);readonly turmas=signal<Turma[]>([]);
  readonly professoresFiltrados=computed(()=>{
    const normalize=(v:string)=>v.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();const busca=normalize(this.busca().trim());
    return this.sort.ordered(this.professores().filter(p=>(!busca||normalize(p.nome+' '+p.login).includes(busca))&&(!this.filtroStatus()||p.ativo===(this.filtroStatus()==='ativo'))&&(!this.filtroTurma()||p.turmas?.some(t=>t.id===this.filtroTurma())||p.materias?.some(m=>m.turmas.some(t=>t.id===this.filtroTurma())))),(p,c)=>c==='status'?(p.ativo?'Ativo':'Inativo'):c==='login'?p.login:c==='materias'?(p.materias??[]).map(m=>m.nome+' '+m.turmas.map(t=>t.ensino+' '+t.ano+' '+t.letra).join(' ')).join(' '):p.nome);
  });
  applyFilters(){if(this.perfil()==='aluno')void this.changeProfile('aluno');}
  clearFilters(){this.busca.set('');this.filtroStatus.set('');this.filtroTurma.set(null);this.applyFilters();}
  readonly professores = signal<Professor[]>([]);
  readonly alunos=signal<Aluno[]>([]);
  paginaAlunos=1; tamanhoAlunos=15; totalAlunos=0;
  readonly usuarios = signal<UsuarioAdministrativo[]>([]);
  readonly perfil = signal('administrador');
  readonly usuariosFiltrados = computed(() => this.perfil() === 'administrador' ? this.sort.ordered(this.usuarios(),(u,c)=>c==='status'?(u.ativo?'Ativo':'Inativo'):c==='login'?u.login:u.nome) : []);
  readonly loading = signal(true);
  readonly saving = signal<number | null>(null);
  readonly erro = signal('');
  readonly mensagem = signal('');

  async ngOnInit() {
    void this.api.get<Turma[]>('/turmas').then(t=>this.turmas.set(t)).catch(()=>this.erro.set('Não foi possível carregar as turmas do filtro.'));
    const perfil=this.route.snapshot.queryParamMap.get('perfil');
    await this.changeProfile(perfil==='professor'||perfil==='aluno'?perfil:'administrador');
  }

  /** Carrega somente os dados do perfil selecionado e ignora respostas de seleções anteriores. */
  async changeProfile(perfil: string, pagina=1) {
    /* A lista de um perfil não pode aparecer sob o cabeçalho de outro durante a troca. */
    const requestId = ++this.requestId;
    if(perfil!==this.perfil()){this.busca.set('');this.filtroStatus.set('');this.filtroTurma.set(null);this.sort.reset();}
    this.perfil.set(perfil);
    this.loading.set(true); this.erro.set(''); this.mensagem.set('');
    this.usuarios.set([]); this.professores.set([]); this.alunos.set([]);this.totalAlunos=0;this.paginaAlunos=pagina;
    try {
      if (perfil === 'administrador') {
        const usuarios = await this.api.get<UsuarioAdministrativo[]>('/administradores');
        if (requestId === this.requestId) this.usuarios.set(usuarios);
      } else if (perfil === 'professor') {
        const professores = await this.api.get<Professor[]>('/professores');
        if (requestId === this.requestId) this.professores.set(professores);
      } else if(perfil==='aluno') {
        const result=await this.api.get<Pagina<Aluno>>('/alunos?pagina='+pagina+'&tamanho='+this.tamanhoAlunos+(this.busca().trim()?'&busca='+encodeURIComponent(this.busca().trim()):'')+(this.filtroStatus()?'&status='+this.filtroStatus():'')+(this.filtroTurma()?'&turmaId='+this.filtroTurma():'')+this.sort.query());
        if(requestId===this.requestId){this.alunos.set(result.itens);this.totalAlunos=result.total;this.paginaAlunos=result.pagina;}
      }
    } catch {
      if (requestId === this.requestId) this.erro.set('Não foi possível carregar os usuários.');
    } finally { if (requestId === this.requestId) this.loading.set(false); }
  }

  changeAlunoPageSize(size:number){this.tamanhoAlunos=size;void this.changeProfile('aluno');}

  /** Ativa ou desativa um professor preservando nome, credenciais e turmas. */
  async toggleProfessor(professor: Professor) {
    /* O bloqueio evita envios simultâneos; a linha só muda após confirmação do servidor. */
    if (this.saving() !== null) return;
    const requestId = this.requestId;
    this.saving.set(professor.id); this.erro.set(''); this.mensagem.set('');
    try {
      const atualizado = await this.api.patch<Professor>('/professores/' + professor.id + '/status', { ativo: !professor.ativo });
      if (requestId !== this.requestId) return;
      this.professores.update(lista => lista.map(item => item.id === atualizado.id ? atualizado : item));
      this.mensagem.set(atualizado.ativo ? 'Professor ativado.' : 'Professor desativado.');
    } catch (error) {
      if (requestId === this.requestId) this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem
        ? error.error.mensagem : 'Não foi possível alterar o status. Tente novamente.');
    } finally { this.saving.set(null); }
  }

  async toggle(usuario: UsuarioAdministrativo) {
    if (this.saving() !== null) return;
    this.saving.set(usuario.id); this.erro.set(''); this.mensagem.set('');
    try {
      const atualizado = await this.api.patch<UsuarioAdministrativo>('/administradores/' + usuario.id + '/status', { ativo: !usuario.ativo });
      this.usuarios.update(lista => lista.map(item => item.id === atualizado.id ? atualizado : item));
      this.mensagem.set(atualizado.ativo ? 'Administrador ativado.' : 'Administrador desativado.');
      if (!atualizado.ativo && atualizado.id === this.auth.usuario()?.id) {
        this.auth.usuario.set(null);
        await this.router.navigateByUrl('/login');
      }
    } catch (error) {
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem ? error.error.mensagem
        : 'Não foi possível alterar o status. Tente novamente.');
    } finally { this.saving.set(null); }
  }
}

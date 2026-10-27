import { statusAluno } from '../../../models/status-aluno';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective, RecordNameDirective } from '../../../design-system/components';
import { PaginationComponent } from '../../../design-system/pagination';
import { Aluno, Pagina } from '../../../models/aluno';
import { Professor } from '../../../models/professor';
import { Turma } from '../../../models/turma';
import { ApiService } from '../../../services/api.service';

@Component({ selector: 'app-listar-alunos',
  imports: [TableScrollComponent, FormsModule, AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective, RecordNameDirective, PaginationComponent],
  templateUrl: './listar.html', styleUrl: '../../../design-system/list-filters.css',
})
export class ListarAlunosComponent implements OnInit {
 readonly statusAluno=statusAluno;
  private readonly api = inject(ApiService);
  readonly alunos = signal<Aluno[]>([]);
  readonly turmas = signal<Turma[]>([]);
  readonly loading = signal(false);
  readonly erro = signal('');
  busca='';status='';
  clearFilters(){this.busca='';this.status='';this.turmaId=null;this.changeTurma();}
  turmaId: number | null = null;
  pagina = 1;
  tamanhoPagina = 15;
  total = 0;
  private visit = 0;
  ngOnInit() { void this.init(); }

  /** Obtém as turmas próprias para o filtro, sem aceitar IDs de outro professor. */
  async init() {
    this.loading.set(true); this.erro.set('');
    /* A lista só é carregada após obter o contexto da sessão. */
    try {
      const professor = await this.api.get<Professor>('/professor/dashboard');
      const turmas = new Map<number,Turma>();
      professor.materias.forEach(m => m.turmas.forEach(t => turmas.set(t.id,t)));
      this.turmas.set([...turmas.values()].sort((a,b) => a.ensino.localeCompare(b.ensino) || a.ano-b.ano || a.letra.localeCompare(b.letra)));
      await this.load();
    } catch { this.loading.set(false); this.erro.set('Não foi possível carregar suas turmas. Tente novamente.'); }
  }

  /** Consulta uma página autorizada pelo servidor e descarta respostas de filtros antigos. */
  async load() {
    /* Alterar filtros rapidamente não permite que uma resposta anterior substitua a pesquisa mais recente. */
    const visit = ++this.visit; this.loading.set(true); this.erro.set(''); this.alunos.set([]);
    const query = '?pagina=' + this.pagina + '&tamanho=' + this.tamanhoPagina + (this.turmaId ? '&turmaId=' + this.turmaId : '')+(this.busca.trim()?'&busca='+encodeURIComponent(this.busca.trim()):'')+(this.status?'&status='+this.status:'');
    try {
      const result = await this.api.get<Pagina<Aluno>>('/professor/alunos' + query);
      if (visit !== this.visit) return;
      this.alunos.set(result.itens); this.total=result.total; this.pagina=result.pagina;
    } catch { if (visit === this.visit) { this.total=0; this.erro.set('Não foi possível carregar os alunos. Tente novamente.'); } }
    finally { if (visit === this.visit) this.loading.set(false); }
  }

  /** Troca o filtro e inicia a busca na primeira página. */
  changeTurma() { this.pagina=1; void this.load(); }
  /** Aplica o limite escolhido e reinicia a navegação. */
  changeSize(size: number) { this.tamanhoPagina=size; this.pagina=1; void this.load(); }
  /** Navega preservando o filtro de turma. */
  changePage(page: number) { this.pagina=page; void this.load(); }
}

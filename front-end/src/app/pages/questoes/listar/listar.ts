import { ListSort } from '../../../design-system/list-sort';
import { SortHeaderComponent } from '../../../design-system/sort-header';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { InfoTooltipComponent } from '../../../design-system/info-tooltip';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective, RecordNameDirective } from '../../../design-system/components';
import { TabsComponent } from '../../../design-system/tabs';
import { PaginationComponent } from '../../../design-system/pagination';
import { QuestionPreviewComponent } from '../../../design-system/question-preview';
import { QuestaoRascunho, QuestaoResumo, QuestaoResponse, QuestaoDestino } from '../../../models/questao';
import { PendenciaQuestoes } from '../../../models/pendencia-questoes';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-listar-questoes',
  imports: [SortHeaderComponent,TableScrollComponent, InfoTooltipComponent, TabsComponent, PaginationComponent, FormsModule, FieldComponent, InputDirective, RouterLink, AlertComponent, ButtonDirective, CardComponent, RecordNameDirective, QuestionPreviewComponent],
  templateUrl: './listar.html',
  styleUrls: ['../../../design-system/list-filters.css', '../../../design-system/question-deficits.css', '../../../design-system/table-detail.css', '../../../design-system/screen-title.css'],
})
export class ListarQuestoesComponent implements OnInit {
 readonly sort=new ListSort();
 readonly pendingSort=new ListSort();

  private readonly api = inject(ApiService);
  readonly questoes = signal<QuestaoResumo[]>([]);
  readonly loading = signal(false);
  readonly erro = signal('');
  readonly detalheErro = signal('');
  readonly opening = signal<number | null>(null);
  readonly expandedId = signal<number | null>(null);
  private detailVisit = 0;
  readonly selecionada = signal<QuestaoRascunho | null>(null);
  readonly destinos = signal<QuestaoDestino[]>([]);
  readonly pendencias = signal<PendenciaQuestoes[]>([]);
  readonly pendenciasErro = signal('');
  aba = 'cadastradas';
  readonly abas = [{ id: 'cadastradas', label: 'Questões cadastradas' }, { id: 'faltantes', label: 'Questões que faltam cadastrar' }];
  changeTab(id: string) { this.aba = id; this.collapseDetail(); }
  assuntoId:number|null=null;
  get assuntosDisponiveis(){return [...new Map(this.questoes().filter(q=>q.assuntoId).map(q=>[q.assuntoId,{id:q.assuntoId,label:q.materia+' — '+q.assunto}])).values()].sort((a,b)=>a.label.localeCompare(b.label,'pt-BR'));}
  ensinoAno = '';
  turma = '';
  pagina = 1;
  tamanhoPagina = 15;
  peso: number | null = null;
  tipo = '';

  get filtrosAtivos() { return !!this.assuntoId || !!this.ensinoAno || !!this.turma || this.peso !== null || !!this.tipo; }
  get pesosDisponiveis() { return [...new Set(this.questoes().map(q => q.peso))].sort((a, b) => a - b); }

  get anosDisponiveis() {
    return [...new Map([...this.destinos(), ...this.pendencias()].map(q => [q.ensino + ':' + q.ano, {
      value: q.ensino + ':' + q.ano, ensino: q.ensino, ano: q.ano,
      label: (q.ensino === 'F' ? 'Fundamental' : 'Médio') + ' — ' + q.ano + 'º ano',
    }])).values()].sort((a, b) => a.ensino.localeCompare(b.ensino) || a.ano - b.ano);
  }

  get turmasDisponiveis() {
    /* As opções da API representam somente os vínculos ativos do professor. */
    return [...new Set(this.destinos().filter(d => d.ensino + ':' + d.ano === this.ensinoAno).map(d => d.letra))].sort();
  }

  get questoesFiltradas() {
    /* Questões do ano inteiro também pertencem a qualquer turma selecionada daquele ano. */
    return this.sort.ordered(this.questoes().filter(q => (!this.ensinoAno || q.ensino + ':' + q.ano === this.ensinoAno)
      && (!this.turma || q.abrangencia === 'ano' || q.letras.split(',').some(l => l.trim() === this.turma))
      && (this.peso === null || q.peso === this.peso)
      && (!this.tipo || q.tipo === this.tipo) && (!this.assuntoId||q.assuntoId===this.assuntoId)),(q,c)=>c==='assunto'?q.assunto:c==='resumo'?q.resumo:c==='turma'?[q.ensino,q.ano,q.abrangencia==='ano'?'Todas as turmas':q.letras]:c==='tipo'?(q.tipo==='objetiva'?'Objetiva':'Discursiva'):c==='peso'?q.peso:q.materia);
  }

  get pendenciasFiltradas() {
    /* As metas são por matéria/turma e peso, considerando questões objetivas e discursivas juntas. */
    return this.pendingSort.ordered(this.pendencias().filter(p => (!this.ensinoAno || p.ensino + ':' + p.ano === this.ensinoAno) && (!this.turma || p.letra === this.turma)),(p,c)=>c==='assunto'?p.assunto:c==='materia'?p.materia:c==='ano'?[p.ensino,p.ano]:c==='turma'?p.letra:p.quantidades.find(q=>q.peso===Number(c))?.faltantes??0);
  }

  get paginaAtual() { return Math.min(this.pagina, Math.max(1, Math.ceil(this.questoesFiltradas.length / this.tamanhoPagina))); }
  get questoesDaPagina() { return this.questoesFiltradas.slice((this.paginaAtual - 1) * this.tamanhoPagina, this.paginaAtual * this.tamanhoPagina); }
  changePageSize(size: number) { this.tamanhoPagina = size; this.closeDetail(); }

  /** Trocar ensino/ano descarta a turma e o detalhe abertos no contexto anterior. */
  changeAno() {
    /* O segundo filtro depende do primeiro e nunca conserva uma turma incompatível. */
    this.turma = ''; this.closeDetail();
  }

  /** Fecha a prévia ao alterar a pesquisa. */
  closeDetail() { this.pagina = 1; this.collapseDetail(); }

  /** Recolhe a linha e descarta consultas que terminarem após o fechamento. */
  collapseDetail() {
    this.detailVisit++; this.expandedId.set(null); this.opening.set(null);
    this.selecionada.set(null); this.detalheErro.set('');
  }

  /** Navega sem manter uma questão aberta de outra página. */
  changePage(page: number) { this.pagina=page; this.collapseDetail(); }

  /** Retorna à lista completa das questões do professor. */
  resetFilters() { this.assuntoId=null;this.ensinoAno = ''; this.peso = null; this.tipo = ''; this.changeAno(); }

  ngOnInit() { void this.load(); }

  /** Busca os resumos das questões do professor autenticado. */
  async load() {
    /* O servidor limita o autor pela sessão e não inclui imagens completas nesta consulta. */
    if (this.loading()) return;
    this.loading.set(true); this.erro.set('');
    try {
      const [questoes, destinos, pendencias] = await Promise.all([
        this.api.get<QuestaoResumo[]>('/questoes'), this.api.get<QuestaoDestino[]>('/questoes/opcoes'),
        this.api.get<PendenciaQuestoes[]>('/professor/dashboard/pendencias-questoes')
          .then(data => ({ data, erro: '' }), () => ({ data: [] as PendenciaQuestoes[], erro: 'Não foi possível carregar as questões faltantes. Tente novamente.' })),
      ]);
      this.questoes.set(questoes); this.destinos.set(destinos);
      this.pendencias.set(pendencias.data); this.pendenciasErro.set(pendencias.erro);
      if (!this.anosDisponiveis.some(a => a.value === this.ensinoAno)) this.resetFilters();
      if (this.turma && !this.turmasDisponiveis.includes(this.turma)) this.turma = '';
    }
    catch { this.erro.set('Não foi possível carregar as questões. Tente novamente.'); }
    finally { this.loading.set(false); }
  }

  /** Carrega o conteúdo completo somente ao solicitar sua visualização. */
  async open(id: number) {
    /* Apenas uma consulta de detalhe é executada por vez; o acesso ao autor é validado na API. */
    if (this.expandedId() === id) { this.collapseDetail(); return; }
    if (this.opening() !== null) return;
    const visit = ++this.detailVisit; this.expandedId.set(id);
    this.opening.set(id); this.detalheErro.set(''); this.selecionada.set(null);
    try {
      const q = await this.api.get<QuestaoResponse>('/questoes/' + id);
      if (visit !== this.detailVisit) return;
      this.selecionada.set({ id: q.id, tipo: q.tipo, peso: q.peso, enunciado: q.enunciado,
        alternativas: { A: '', B: '', C: '', D: '', E: '', ...q.alternativas }, respostaCorreta: q.respostaCorreta ?? 'A' });
    } catch { if (visit === this.detailVisit) this.detalheErro.set('Não foi possível abrir esta questão.'); }
    finally { if (visit === this.detailVisit) this.opening.set(null); }
  }
}

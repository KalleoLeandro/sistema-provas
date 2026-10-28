import { Assunto } from '../../../models/assunto';
import { InfoTooltipComponent } from '../../../design-system/info-tooltip';
import { HttpErrorResponse } from '@angular/common/http';
import { AccordionComponent } from '../../../design-system/accordion';
import { Component, ElementRef, HostListener, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormActionsComponent } from '../../../design-system/form-actions';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective } from '../../../design-system/components';
import { RichTextEditorComponent } from '../../../design-system/extensions/rich-text-editor';
import { QuestionPreviewComponent } from '../../../design-system/question-preview';
import { AllowedValuesDirective, RichTextRequiredDirective, ValidationMessagePipe } from '../../../design-system/validators';
import { LetraAlternativa, QuestaoRascunho, QuestaoResponse, QuestaoCadastrada, QuestaoDestino } from '../../../models/questao';
import { ChipSelectComponent } from '../../../design-system/chip-select';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-cadastrar-questoes',
  imports: [InfoTooltipComponent, FormsModule, RouterLink, AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective,
    RichTextEditorComponent, QuestionPreviewComponent, AccordionComponent, FormActionsComponent, ChipSelectComponent, AllowedValuesDirective, RichTextRequiredDirective, ValidationMessagePipe],
  templateUrl: './cadastrar.html',
  styleUrls: ['../../../design-system/question-form.css', '../../../design-system/screen-title.css'],
})
export class CadastrarQuestoesComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  readonly editId = inject(ActivatedRoute).snapshot.paramMap.get('id');
  private versaoId: number | null = null;
  private revisao: number | null = null;
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private nextId = 1;
  private visit = 0;
  readonly letras: LetraAlternativa[] = ['A', 'B', 'C', 'D', 'E'];
  readonly pesos = [0.5, 1, 1.5, 2];
  readonly tipos = ['objetiva', 'discursiva'];
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly cadastradas = signal<QuestaoCadastrada[]>([]);
  readonly erro = signal('');
  readonly mensagem = signal('');
  readonly destinos = signal<QuestaoDestino[]>([]);
  materiaId: number | null = null;
  anoSelecionado = '';
  turmasSelecionadas: number[] = [];

  /** Turmas vazias representam o ano inteiro, sem exigir outra seleção na tela. */
  assuntos:Assunto[]=[];assuntoSelecionado:number[]=[];
  readonly loadingAssuntos=signal(false);readonly creatingAssunto=signal(false);private assuntoVisit=0;
  get opcoesAssuntos(){return this.assuntos.map(a=>({value:a.id,label:a.nome}));}
  async loadAssuntos(){const materia=this.materiaId,visit=++this.assuntoVisit;this.assuntos=[];this.loadingAssuntos.set(true);
    try{if(materia){const rows=await this.api.get<Assunto[]>('/professor/assuntos?materiaId='+materia);if(visit===this.assuntoVisit)this.assuntos=rows;}}
    catch{if(visit===this.assuntoVisit)this.erro.set('Não foi possível carregar os assuntos.');}finally{if(visit===this.assuntoVisit)this.loadingAssuntos.set(false);}}
  async createAssunto(nome:string){if(!this.materiaId||this.creatingAssunto())return;const materia=this.materiaId,visit=this.assuntoVisit;this.creatingAssunto.set(true);
    try{const a=await this.api.post<Assunto>('/professor/assuntos',{materiaId:materia,nome});if(visit!==this.assuntoVisit)return;this.assuntos=[...this.assuntos.filter(x=>x.id!==a.id),a].sort((a,b)=>a.nome.localeCompare(b.nome,'pt-BR'));this.assuntoSelecionado=[a.id];}
    catch{if(visit===this.assuntoVisit)this.erro.set('Não foi possível criar o assunto. Tente novamente.');}finally{this.creatingAssunto.set(false);}}
  get abrangencia(): 'turmas' | 'ano' {
    /* O escopo é derivado dos chips para manter o envio consistente com o formulário. */
    return this.turmasSelecionadas.length ? 'turmas' : 'ano';
  }

  get materiasDisponiveis() { return [...new Map(this.destinos().map(d => [d.materiaId, { id: d.materiaId, nome: d.materia }])).values()]; }
  get anosDisponiveis() {
    return [...new Map(this.destinos().filter(d => d.materiaId === this.materiaId).map(d => [d.ensino + ':' + d.ano,
      { value: d.ensino + ':' + d.ano, ensino: d.ensino, ano: d.ano, label: (d.ensino === 'F' ? 'Fundamental' : 'Médio') + ' — ' + d.ano + 'º ano' }])).values()];
  }
  get anoAtual() { return this.anosDisponiveis.find(a => a.value === this.anoSelecionado); }
  get turmasDisponiveis() { return this.destinos().filter(d => d.materiaId === this.materiaId && d.ensino + ':' + d.ano === this.anoSelecionado).map(d => ({ value: d.turmaId, label: d.letra })); }

  /** Descarta ano e turmas ao trocar a matéria para não reaproveitar uma seleção incompatível. */
  changeMateria() { /* A nova matéria determina os anos disponíveis. */ this.anoSelecionado = ''; this.turmasSelecionadas = [];this.assuntoSelecionado=[];void this.loadAssuntos(); }
  /** Mantém as turmas selecionadas dentro de um único ensino e ano. */
  changeAno() { /* Qualquer seleção anterior é descartada na mudança de contexto. */ this.turmasSelecionadas = []; }

  preview = true;
  questoes = [this.newQuestion()];

  ngOnInit() { void this.load(); }

  /** Obtém os vínculos já existentes do professor, apenas para preencher a interface. */
  async load() {
    /* A API fornece matérias e anos autorizados, com as turmas escolares do mesmo ano. */
    if (this.loading()) return;
    const visit=this.visit;this.loading.set(true); this.erro.set('');
    try {
      const destinos=await this.api.get<QuestaoDestino[]>('/questoes/opcoes');if(visit!==this.visit)return;this.destinos.set(destinos);
      if (this.editId) {
        /* O formulário de edição usa a versão lida para detectar alterações simultâneas. */
        const q = await this.api.get<QuestaoResponse>('/questoes/' + this.editId);if(visit!==this.visit)return;
        this.versaoId = q.versaoId; this.revisao = q.revisao; this.materiaId = q.materiaId;this.assuntoSelecionado=[q.assuntoId];await this.loadAssuntos();if(visit!==this.visit)return;
        this.anoSelecionado = q.ensino + ':' + q.ano;
        this.turmasSelecionadas = q.abrangencia === 'ano' ? [] : q.turmas.map(t => t.id);
        this.questoes = [{ id: q.id, tipo: q.tipo, peso: q.peso, enunciado: q.enunciado,
          alternativas: { A: '', B: '', C: '', D: '', E: '', ...q.alternativas }, respostaCorreta: q.respostaCorreta ?? 'A' }];
      }
    } catch { this.erro.set('Não foi possível carregar suas matérias e turmas.'); }
    finally { this.loading.set(false); }
  }

  /** Cria uma questão independente usando os campos disponíveis no projeto anterior. */
  private newQuestion(): QuestaoRascunho {
    /* O identificador existe apenas para manter os editores estáveis durante a edição local. */
    return { id: this.nextId++, tipo: 'objetiva', peso: 1, enunciado: '',
      alternativas: { A: '', B: '', C: '', D: '', E: '' }, respostaCorreta: 'A' };
  }

  /** Atualiza o rascunho e entrega uma nova referência para a prévia acompanhar a digitação. */
  updateQuestion(id: number, changes: Partial<QuestaoRascunho>) {
    /* Mantém o identificador estável para não recriar nem perder o foco dos editores. */
    this.questoes = this.questoes.map(q => q.id === id ? { ...q, ...changes } : q);
    this.mensagem.set('');
  }

  /** Atualiza somente a alternativa editada, preservando as demais. */
  updateAlternative(id: number, letra: LetraAlternativa, html: string) {
    /* Cada questão conserva seu próprio conjunto de alternativas. */
    const questao = this.questoes.find(q => q.id === id);
    if (questao) this.updateQuestion(id, { alternativas: { ...questao.alternativas, [letra]: html } });
  }

  /** Grava a questão e retorna à lista ou prepara um novo cadastro na mesma tela. */
  async save(form: NgForm, continuar = false) {
    /* Apenas uma questão é enviada por vez; o bloqueio impede cadastros repetidos por duplo clique. */
    if (this.saving() || this.loading() || this.loadingAssuntos() || this.creatingAssunto()) return;
    this.erro.set('');
    form.control.markAllAsTouched();
    const ano = this.anoAtual;
    if (form.invalid || this.assuntoSelecionado.length!==1 || !this.assuntos.some(a=>a.id===this.assuntoSelecionado[0]) || !ano || !this.materiaId || this.turmasSelecionadas.some(id => !this.turmasDisponiveis.some(t => t.value === id))) {
      this.mensagem.set('Preencha os campos destacados para cadastrar a questão.');
      return;
    }
    const q = this.questoes[0];
    const materiaId = this.materiaId;
    const materia = this.materiasDisponiveis.find(m => m.id === materiaId)!;
    const descricao = materia.nome + ' — ' + (this.assuntos.find(a=>a.id===this.assuntoSelecionado[0])?.nome??'') + ' — ' + ano.label + ' — ' + (this.abrangencia === 'ano' ? 'Todas as turmas' : 'Turmas ' + this.turmasDisponiveis.filter(t => this.turmasSelecionadas.includes(t.value)).map(t => t.label).join(', '));
    this.saving.set(true); this.mensagem.set('');
    const visit = this.visit;
    try {
      /* O accordion usa a resposta do banco e só aparece após a confirmação de gravação. */
      const dados = {
        materiaId, assuntoId:this.assuntoSelecionado[0], ensino: ano.ensino, ano: ano.ano, abrangencia: this.abrangencia, turmas: this.abrangencia === 'ano' ? [] : [...this.turmasSelecionadas], tipo: q.tipo, peso: q.peso, enunciado: q.enunciado,
        alternativas: q.tipo === 'objetiva' ? { ...q.alternativas } : {},
        respostaCorreta: q.tipo === 'objetiva' ? q.respostaCorreta : null,
      };
      const response = this.editId
        ? await this.api.put<QuestaoResponse>('/questoes/' + this.editId, { versaoId: this.versaoId, revisao: this.revisao, questao: dados })
        : await this.api.post<QuestaoResponse>('/questoes', dados);
      if (visit !== this.visit) return;
      /* Cadastrar conclui o fluxo na lista; continuar conserva os accordions apenas nesta visita. */
      if (this.editId || !continuar) {
        await this.router.navigateByUrl('/questoes/listar');
        return;
      }
      const questao: QuestaoRascunho = { id: response.id, tipo: response.tipo, peso: response.peso,
        enunciado: response.enunciado, alternativas: { A: '', B: '', C: '', D: '', E: '', ...response.alternativas },
        respostaCorreta: response.respostaCorreta ?? 'A' };
      const document = new DOMParser().parseFromString(response.enunciado, 'text/html');
      document.querySelectorAll('img,svg,picture,script,style').forEach(node => node.remove());
      const texto = document.body.textContent?.replace(/[\p{So}\uFE0E\uFE0F\u200D]/gu, '').replace(/\s+/g, ' ').trim() ?? '';
      this.cadastradas.update(lista => [...lista, { questao, vinculo: descricao, resumo: texto.length > 120 ? texto.slice(0, 117).trimEnd() + '...' : texto || 'Questão com imagem' }]);
      /* Mantém matéria/turma e abre um novo editor, sem perder os registros já cadastrados nesta tela. */
      this.questoes = [this.newQuestion()];
      form.resetForm({ assuntos:[...this.assuntoSelecionado], materiaId: this.materiaId, anoSelecionado: this.anoSelecionado, turmas: [...this.turmasSelecionadas] });
      this.mensagem.set('Questão cadastrada com sucesso. Preencha a próxima.');
    } catch (error) {
      /* Falhas preservam o rascunho completo e não acrescentam questões à lista de confirmadas. */
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem ? error.error.mensagem : 'Não foi possível cadastrar a questão. Tente novamente.');
    } finally { this.saving.set(false); }
  }

  /** Limpa o formulário atual sem remover as questões já confirmadas nesta tela. */
  reset(form: NgForm) {
    /* Os dados não ficam em storage; o formulário retorna ao estado inicial. */
    if (this.saving()) return;
    this.questoes = [this.newQuestion()]; this.assuntoVisit++;this.loadingAssuntos.set(false);this.assuntos=[];this.assuntoSelecionado=[];this.materiaId = null; this.anoSelecionado = ''; this.turmasSelecionadas = []; this.erro.set(''); this.mensagem.set('');
    form.resetForm({ assuntos:[],materiaId: null, anoSelecionado: '', turmas: [] }); this.preview = true;
  }

  /** Descarta os dados locais ao sair da página ou fechar a aba. */
  @HostListener('window:pagehide')
  clear() {
    /* Imagens inseridas também são descartadas junto com o HTML do rascunho. */
    this.visit++;
    this.materiaId = null; this.anoSelecionado = ''; this.turmasSelecionadas = []; this.questoes = [this.newQuestion()]; this.cadastradas.set([]); this.mensagem.set('');
  }
  ngOnDestroy() { this.clear(); }
}

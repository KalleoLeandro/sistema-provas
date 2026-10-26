import { ImportarAlunosComponent } from './importar-alunos';
import { TabsComponent } from '../../../design-system/tabs';
import { AllowedValuesDirective, NotBlankDirective, TrimmedMinlengthDirective, ValidationMessagePipe } from '../../../design-system/validators';
﻿import { Component, HostListener, OnDestroy, inject, signal, computed, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { FormActionsComponent } from '../../../design-system/form-actions';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { Turma } from '../../../models/turma';
import { ChipSelectComponent } from '../../../design-system/chip-select';
import { Materia } from '../../../models/materia';
import { AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective } from '../../../design-system/components';

@Component({
  selector: 'app-cadastrar-usuario',
  imports: [ImportarAlunosComponent, TabsComponent, AllowedValuesDirective, NotBlankDirective, TrimmedMinlengthDirective, ValidationMessagePipe, FormsModule, AlertComponent, ButtonDirective, CardComponent, FieldComponent, InputDirective, FormActionsComponent, ChipSelectComponent],
  templateUrl: './cadastrar.html',
  styleUrl: './cadastrar.css',
})
export class CadastrarUsuarioComponent implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  nome = '';
  numero: number | null = null;
  cgm = '';
  statusAluno = 'M';
  turmaId: number | null = null;
  login = '';
  senha = '';
  materias: number[] = [];
  turmasPorMateria: Record<number, number[]> = {};
  perfil = 'administrador';
  modoAluno='individual';
  readonly modosAluno=[{id:'individual',label:'Cadastro individual'},{id:'csv',label:'Importar CSV'}];
  changeAlunoMode(mode:string,form:NgForm){if(this.busy())return;this.reset(form);this.modoAluno=mode;}
  readonly turmasDisponiveis = signal<Turma[] | null>(null);
  readonly materiasDisponiveis = signal<Materia[] | null>(null);
  readonly loadingCatalogos = signal(false);
  readonly opcoesMaterias = computed(() => (this.materiasDisponiveis() ?? []).map(m => ({ value: m.id, label: m.nome })));
  readonly opcoesTurmas = computed(() => (this.turmasDisponiveis() ?? []).map(t => ({
    value: t.id, label: `${t.ensino === 'M' ? 'Médio' : 'Fundamental'} — ${t.ano}º ano ${t.letra}`,
  })));
  readonly busy = signal(false);
  readonly erro = signal('');
  readonly mensagem = signal('');

  /** Troca o formulário de perfil sem reaproveitar dados pessoais ou credenciais. */
  async changeProfile(form: NgForm) {
    /* A troca só é permitida fora de um envio; turmas são carregadas sob demanda. */
    if (this.busy()) return;
    const perfil = this.perfil;this.modoAluno='individual';
    this.reset(form);
    this.perfil = perfil;
    if (perfil === 'professor' && (this.turmasDisponiveis() === null || this.materiasDisponiveis() === null)) await this.loadCatalogos();
    if (perfil === 'aluno' && this.turmasDisponiveis() === null) await this.loadTurmas();
  }

  /** Carrega os catálogos existentes para selecionar matérias e turmas por seus IDs. */
  async loadCatalogos() {
    /* Falhas permitem nova tentativa sem perder a sessão ou enviar referências livres. */
    if (this.loadingCatalogos()) return;
    this.loadingCatalogos.set(true); this.erro.set('');
    try {
      const [turmas, materias] = await Promise.all([this.api.get<Turma[]>('/turmas'), this.api.get<Materia[]>('/materias')]);
      this.turmasDisponiveis.set(turmas); this.materiasDisponiveis.set(materias);
    } catch {
      this.erro.set('Não foi possível carregar as matérias e turmas. Tente novamente.');
    } finally { this.loadingCatalogos.set(false); }
  }

  /** Carrega apenas as turmas necessárias ao cadastro administrativo do aluno. */
  async loadTurmas() {
    if (this.loadingCatalogos()) return;
    this.loadingCatalogos.set(true);this.erro.set('');
    try { this.turmasDisponiveis.set(await this.api.get<Turma[]>('/turmas')); }
    catch { this.erro.set('Não foi possível carregar as turmas. Tente novamente.'); }
    finally { this.loadingCatalogos.set(false); }
  }

  /** Mantém as turmas somente das matérias que continuam selecionadas. */
  changeMaterias(ids: number[]) {
    /* Remover uma matéria descarta suas turmas; selecioná-la novamente começa sem vínculos. */
    this.materias = [...ids];
    this.turmasPorMateria = Object.fromEntries(ids.map(id => [id, this.turmasPorMateria[id] ?? []]));
  }

  /** Obtém o rótulo da matéria para identificar sua seleção de turmas. */
  materiaNome(id: number): string {
    /* O nome é lido do catálogo, sem aceitar descrição livre no cadastro. */
    return this.materiasDisponiveis()?.find(m => m.id === id)?.nome ?? '';
  }

  /** Prepara o formulário para um novo cadastro mantendo o perfil selecionado. */
  reset(form: NgForm) {
    /* O status inicial é definido pelo banco e nenhuma turma permanece selecionada entre cadastros. */
    if (this.busy()) return;
    this.clear();
    form.resetForm({ perfil: this.perfil, nome: '', login: '', senha: '', materias: [], numero: null, cgm: '', statusAluno: 'M', turmaId: null });
    this.erro.set(''); this.mensagem.set('');
  }

  /** Valida e cadastra o perfil escolhido, retornando à lista ou preparando o próximo cadastro. */
  async save(form: NgForm, continuar = false) {
    /* Campos obrigatórios e carregamento das turmas devem estar resolvidos antes do envio. */
    if (this.busy()) return;
    if (this.perfil === 'aluno') { if(this.modoAluno==='individual')await this.saveAluno(form, continuar); return; }
    if (form.invalid || this.login.trim().length < 3 || !this.senha.trim()
      || (this.perfil === 'professor' && (!this.nome.trim() || !this.materias.length || this.turmasDisponiveis() === null || this.materiasDisponiveis() === null || this.loadingCatalogos()))) {
      form.control.markAllAsTouched();
      this.erro.set('Preencha os campos obrigatórios e informe uma senha de 8 a 128 caracteres.');
      return;
    }
    this.busy.set(true);
    this.erro.set(''); this.mensagem.set('');
    const professor = this.perfil === 'professor';
    try {
      /* O array contém somente IDs. A API verifica a existência das turmas e protege a senha. */
      await this.api.post(professor ? '/professores' : '/administradores', professor
        ? { nome: this.nome.trim(), login: this.login.trim(), senha: this.senha, materias: this.materias.map(materiaId => ({ materiaId, turmas: [...(this.turmasPorMateria[materiaId] ?? [])] })) }
        : { login: this.login.trim(), senha: this.senha });
      this.clear();
      form.resetForm({ perfil: this.perfil, nome: '', login: '', senha: '', materias: [], numero: null, cgm: '', statusAluno: 'M', turmaId: null });
      this.mensagem.set(professor ? 'Professor cadastrado com sucesso.' : 'Administrador cadastrado com sucesso.');
      /* Continuar mantém o perfil; o cadastro comum abre a lista no mesmo perfil salvo. */
      if (continuar) queueMicrotask(() => this.element.nativeElement.querySelector('input')?.focus());
      else await this.router.navigateByUrl('/cadastro/usuarios/listar' + (professor ? '?perfil=professor' : ''));
    } catch (error) {
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem
        ? error.error.mensagem : error instanceof HttpErrorResponse && error.status === 401
        ? 'Sua sessão expirou. Entre novamente.' : 'Não foi possível cadastrar o usuário. Tente novamente.');
    } finally {
      /* A senha é descartada inclusive em falhas; os demais dados permitem corrigir o cadastro. */
      this.senha = '';
      this.busy.set(false);
    }
  }

  /** Cadastra o aluno pelo endpoint administrativo e respeita cadastrar ou cadastrar e continuar. */
  private async saveAluno(form: NgForm, continuar: boolean) {
    /* Nome, CGM, status e turma são obrigatórios; o número fica nulo quando não informado. */
    if (form.invalid || !this.nome.trim() || !/^[0-9]{1,30}$/.test(this.cgm.trim()) || !this.turmaId || this.loadingCatalogos() || this.turmasDisponiveis()===null) {
      form.control.markAllAsTouched();this.erro.set('Preencha os campos obrigatórios do aluno.');return;
    }
    this.busy.set(true);this.erro.set('');this.mensagem.set('');
    try {
      await this.api.post('/alunos',{nome:this.nome.trim(),numero:this.numero??null,cgm:this.cgm.trim(),status:this.statusAluno,turmaId:this.turmaId});
      /* Limpa dados pessoais após confirmar a gravação, mantendo somente o perfil escolhido. */
      this.clear();
      form.resetForm({perfil:'aluno',nome:'',numero:null,cgm:'',statusAluno:'M',turmaId:null});
      this.mensagem.set('Aluno cadastrado com sucesso.');
      if(continuar) queueMicrotask(()=>this.element.nativeElement.querySelector<HTMLInputElement>('#aluno-nome')?.focus());
      else await this.router.navigateByUrl('/cadastro/usuarios/listar?perfil=aluno');
    } catch(error) {
      /* Falhas mantêm os campos para corrigir uma duplicação de CGM ou outra informação recusada. */
      this.erro.set(error instanceof HttpErrorResponse && error.error?.mensagem ? error.error.mensagem : 'Não foi possível cadastrar o aluno. Tente novamente.');
    } finally {this.busy.set(false);}
  }

  /** Descarta todos os valores do cadastro ao limpar, sair ou ocultar a página. */
  @HostListener('window:pagehide')
  clear() {
    /* Dados pessoais e seleções não são persistidos no navegador. */
    this.nome = ''; this.numero=null;this.cgm='';this.statusAluno='M';this.turmaId=null; this.login = ''; this.senha = ''; this.materias = []; this.turmasPorMateria = {};
  }
  ngOnDestroy() { this.clear(); }
}

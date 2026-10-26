import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { By } from '@angular/platform-browser';
import { NgForm } from '@angular/forms';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { CadastrarQuestoesComponent } from './cadastrar';

const response = { id: 101, versaoId: 1001, revisao: 1, materiaId: 6, ensino: 'F', ano: 1, abrangencia: 'turmas', turmas: [{ id: 5, ensino: 'F', ano: 1, letra: 'A' }], tipo: 'discursiva', peso: 1,
  enunciado: '<p>Explique sua resposta.</p>', alternativas: {}, respostaCorreta: null };
describe('Cadastro múltiplo de questões', () => {
  const get = vi.fn(); const post = vi.fn();
  beforeEach(() => {
    get.mockReset().mockResolvedValue([{ materiaId: 6, materia: 'Matemática', turmaId: 5, ensino: 'F', ano: 1, letra: 'A' }, { materiaId: 6, materia: 'Matemática', turmaId: 13, ensino: 'F', ano: 1, letra: 'B' }]);
    post.mockReset().mockResolvedValue(response);
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: ApiService, useValue: { get, post } }] });
  });
  async function validForm() {
    const fixture = TestBed.createComponent(CadastrarQuestoesComponent); await fixture.whenStable();
    const c = fixture.componentInstance; c.materiaId = 6; c.anoSelecionado = 'F:1'; c.turmasSelecionadas = [5]; fixture.detectChanges(); await fixture.whenStable();
    const tipo = fixture.nativeElement.querySelector('select[id^="tipo-"]') as HTMLSelectElement;
    tipo.value = 'discursiva'; tipo.dispatchEvent(new Event('change')); await fixture.whenStable();
    const form = fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm);
    const id = fixture.componentInstance.questoes[0].id;
    form.controls['enunciado-' + id].setValue(response.enunciado); await fixture.whenStable();
    return { fixture, form };
  }
  it('carrega os vínculos sem recarregar questões salvas de visitas anteriores', async () => {
    const fixture = TestBed.createComponent(CadastrarQuestoesComponent); await fixture.whenStable();
    expect(get).toHaveBeenCalledExactlyOnceWith('/questoes/opcoes');
    expect(fixture.nativeElement.querySelectorAll('visual-rich-text-editor')).toHaveLength(6);
    expect(fixture.componentInstance.cadastradas()).toEqual([]);
    expect(post).not.toHaveBeenCalled();
  });
  it('grava, recolhe em accordion e mantém a matéria/turma para a próxima questão', async () => {
    const { fixture, form } = await validForm();
    await fixture.componentInstance.save(form, true); await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/questoes', { materiaId: 6, ensino: 'F', ano: 1, abrangencia: 'turmas', turmas: [5], tipo: 'discursiva', peso: 1,
      enunciado: response.enunciado, alternativas: {}, respostaCorreta: null });
    const accordion = fixture.nativeElement.querySelector('visual-accordion details') as HTMLDetailsElement;
    expect(accordion).toBeTruthy(); expect(accordion.open).toBe(false);
    expect(accordion.textContent).toContain('Explique sua resposta.');
    expect(fixture.componentInstance.cadastradas()[0].questao.id).toBe(101);
    expect(fixture.componentInstance.turmasSelecionadas).toEqual([5]);
    expect(fixture.componentInstance.questoes[0].enunciado).toBe('');
    expect(fixture.nativeElement.querySelectorAll('visual-rich-text-editor')).toHaveLength(6);
  });
  it('Cadastrar salva e redireciona para a lista sem adicionar accordion', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const { fixture, form } = await validForm();
    await fixture.componentInstance.save(form); await fixture.whenStable();
    expect(post).toHaveBeenCalledTimes(1);
    expect(navigate).toHaveBeenCalledWith('/questoes/listar');
    expect(fixture.componentInstance.cadastradas()).toHaveLength(0);
  });
  it('preserva o conteúdo e não cria accordion quando o banco recusa o cadastro', async () => {
    post.mockRejectedValue(new HttpErrorResponse({ status: 503, error: { mensagem: 'Servidor indisponível.' } }));
    const { fixture, form } = await validForm();
    await fixture.componentInstance.save(form, true); await fixture.whenStable();
    expect(fixture.componentInstance.questoes[0].enunciado).toBe(response.enunciado);
    expect(fixture.componentInstance.cadastradas()).toHaveLength(0);
    expect(fixture.componentInstance.erro()).toBe('Servidor indisponível.');
    expect(fixture.componentInstance.saving()).toBe(false);
  });
  it('bloqueia duplo envio e só acrescenta o accordion após confirmação', async () => {
    let confirm!: (value: typeof response) => void;
    post.mockReturnValue(new Promise(resolve => { confirm = resolve; }));
    const { fixture, form } = await validForm();
    const pending = fixture.componentInstance.save(form, true);
    await fixture.componentInstance.save(form, true);
    expect(post).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.cadastradas()).toHaveLength(0);
    confirm(response); await pending; await fixture.whenStable();
    expect(fixture.componentInstance.cadastradas()).toHaveLength(1);
  });
  it('limpar mantém os cadastros da tela e sair descarta somente a lista visual', async () => {
    const { fixture, form } = await validForm();
    await fixture.componentInstance.save(form, true); await fixture.whenStable();
    fixture.componentInstance.reset(form); await fixture.whenStable();
    expect(fixture.componentInstance.cadastradas()).toHaveLength(1);
    fixture.destroy();
    expect(fixture.componentInstance.cadastradas()).toHaveLength(0);
    expect(post).toHaveBeenCalledTimes(1);
  });
  it('não envia questões incompletas', async () => {
    const fixture = TestBed.createComponent(CadastrarQuestoesComponent); await fixture.whenStable();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })); await fixture.whenStable();
    expect(post).not.toHaveBeenCalled(); expect(fixture.nativeElement.querySelectorAll('.campo-erro').length).toBeGreaterThan(0);
  });
  it('envia várias turmas do mesmo ano em uma única questão', async () => {
    const { fixture, form } = await validForm();
    fixture.componentInstance.turmasSelecionadas = [5, 13];
    await fixture.componentInstance.save(form, true);
    expect(post).toHaveBeenCalledExactlyOnceWith('/questoes', expect.objectContaining({ materiaId: 6, ensino: 'F', ano: 1, abrangencia: 'turmas', turmas: [5, 13] }));
  });
  it('chips vazios enviam ano inteiro e mantêm matéria e ano obrigatórios', async () => {
    const { fixture, form } = await validForm();
    fixture.componentInstance.turmasSelecionadas = [];
    fixture.detectChanges(); await fixture.whenStable();
    await fixture.componentInstance.save(form, true);
    expect(post).toHaveBeenCalledExactlyOnceWith('/questoes', expect.objectContaining({ materiaId: 6, ensino: 'F', ano: 1, abrangencia: 'ano', turmas: [] }));
  });
  it('trocar matéria ou ano descarta as turmas anteriores', async () => {
    const { fixture } = await validForm(); const c = fixture.componentInstance;
    c.changeAno(); expect(c.turmasSelecionadas).toEqual([]);
    c.turmasSelecionadas = [5]; c.changeMateria();
    expect(c.anoSelecionado).toBe(''); expect(c.turmasSelecionadas).toEqual([]);
  });

  it('remover o último chip aplica ao ano inteiro e mantém o campo visível e opcional', async () => {
    const { fixture, form } = await validForm();
    form.controls['turmas'].setValue([]); await fixture.whenStable();
    expect(form.valid).toBe(true);
    expect(fixture.nativeElement.querySelector('visual-chip-select')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#questoes-abrangencia')).toBeNull();
    await fixture.componentInstance.save(form, true);
    expect(post).toHaveBeenCalledWith('/questoes', expect.objectContaining({ abrangencia: 'ano', turmas: [] }));
  });

  it('carrega a questão existente e salva com PUT e a versão lida, sem duplicar cadastro', async () => {
    TestBed.overrideProvider(ActivatedRoute, { useValue: { snapshot: { paramMap: { get: () => '101' } } } });
    get.mockImplementation((path: string) => Promise.resolve(path === '/questoes/opcoes'
      ? [{materiaId:6,materia:'Matemática',turmaId:5,ensino:'F',ano:1,letra:'A'}] : response));
    const put = vi.fn().mockResolvedValue({...response,versaoId:1002});
    Object.assign(TestBed.inject(ApiService), { put });
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = TestBed.createComponent(CadastrarQuestoesComponent); await fixture.whenStable();
    const form = fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm);
    expect(fixture.componentInstance.questoes[0].enunciado).toBe(response.enunciado);
    expect(fixture.nativeElement.querySelector('h2').textContent).toBe('Editar questão');
    expect(fixture.nativeElement.querySelector('visual-form-actions')).toBeNull();
    await fixture.componentInstance.save(form);
    expect(put).toHaveBeenCalledWith('/questoes/101', expect.objectContaining({versaoId:1001,revisao:1,questao:expect.objectContaining({materiaId:6,turmas:[5]})}));
    expect(post).not.toHaveBeenCalled(); expect(navigate).toHaveBeenCalledWith('/questoes/listar');
  });

});

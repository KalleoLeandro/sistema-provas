import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { ListarQuestoesComponent } from './listar';

describe('Lista de questões', () => {
  const get = vi.fn();
  beforeEach(() => {
    get.mockReset().mockImplementation((path: string) => Promise.resolve(path === '/questoes/opcoes' || path.endsWith('/pendencias-questoes') ? [] : [{ id: 10, materia: 'Matemática', ensino: 'F', ano: 1, letras: 'A', abrangencia: 'turmas', tipo: 'objetiva', peso: 1, resumo: 'Quanto é 2 + 2?' }]));
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: ApiService, useValue: { get } }] });
  });
  it('lista resumos e carrega o conteúdo completo somente ao visualizar', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    expect(get).toHaveBeenCalledWith('/questoes');
    expect(get).toHaveBeenCalledWith('/questoes/opcoes');
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Quanto é 2 + 2?');
    expect(fixture.nativeElement.querySelector('visual-question-preview')).toBeNull();
    get.mockResolvedValueOnce({ id: 10, materiaId: 6, ensino: 'F', ano: 1, abrangencia: 'turmas', turmas: [{ id: 5, ensino: 'F', ano: 1, letra: 'A' }], tipo: 'objetiva', peso: 1, enunciado: '<p>Quanto é 2 + 2?</p>', alternativas: { A: '1', B: '2', C: '4', D: '5', E: '6' }, respostaCorreta: 'C' });
    (fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(get).toHaveBeenLastCalledWith('/questoes/10');
    expect(fixture.nativeElement.querySelector('visual-question-preview')).toBeTruthy();
    expect(fixture.componentInstance.selecionada()?.respostaCorreta).toBe('C');
    const row=fixture.nativeElement.querySelector('tbody tr');
    expect(row.nextElementSibling.querySelector('visual-question-preview')).toBeTruthy();
    const button=row.querySelector('button');
    expect(button.textContent).toBe('Recolher'); expect(button.getAttribute('aria-expanded')).toBe('true');
    const calls=get.mock.calls.length; button.click(); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.table-detail-row')).toBeNull();
    expect(button.textContent).toBe('Visualizar'); expect(get).toHaveBeenCalledTimes(calls);
  });
  it('permite tentar novamente quando a lista falha e mostra estado vazio', async () => {
    get.mockRejectedValueOnce(new Error('Falha'));
    get.mockImplementation(() => Promise.resolve([]));
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeTruthy();
    (fixture.nativeElement.querySelector('visual-aviso + button') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Nenhuma questão cadastrada.');
    expect(fixture.nativeElement.querySelector('[role=alert]')).toBeNull();
  });
  it('escalona ensino e ano antes da turma e inclui questões do ano inteiro', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c = fixture.componentInstance; const base = c.questoes()[0];
    c.questoes.set([{...base,id:1,letras:'A'}, {...base,id:2,letras:'B'}, {...base,id:3,abrangencia:'ano',letras:''},
      {...base,id:4,ensino:'M'}, {...base,id:5,ano:2}]);
    c.destinos.set([{materiaId:6,materia:'Matemática',turmaId:13,ensino:'F',ano:1,letra:'B'}]);
    expect(fixture.nativeElement.querySelector('#questoes-filtro-turma').disabled).toBe(true);
    c.ensinoAno = 'F:1'; c.changeAno(); c.turma = 'B'; fixture.detectChanges();
    expect(c.questoesFiltradas.map(q => q.id)).toEqual([2,3]);
    expect(c.turmasDisponiveis).toEqual(['B']);
    c.ensinoAno = 'M:1'; c.changeAno(); expect(c.turma).toBe('');
    expect(c.questoesFiltradas.map(q => q.id)).toEqual([4]);
    c.resetFilters(); expect(c.questoesFiltradas).toHaveLength(5);
  });

  it('combina peso e tipo com ano e turma e limpa todos os filtros', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c = fixture.componentInstance; const base = c.questoes()[0];
    c.questoes.set([{...base,id:1,peso:0.5}, {...base,id:2,peso:2,tipo:'discursiva',abrangencia:'ano'},
      {...base,id:3,peso:2,tipo:'discursiva',letras:'B'}, {...base,id:4,peso:2,tipo:'discursiva',ano:2}]);

    c.peso = 2; c.tipo = 'discursiva';
    expect(c.questoesFiltradas.map(q => q.id)).toEqual([2,3,4]);
    c.ensinoAno = 'F:1'; c.turma = 'A';
    expect(c.questoesFiltradas.map(q => q.id)).toEqual([2]);
    c.peso = 0.5; fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Nenhuma questão encontrada para os filtros selecionados.');
    c.resetFilters();
    expect(c.peso).toBeNull(); expect(c.tipo).toBe(''); expect(c.filtrosAtivos).toBe(false);
    expect(c.questoesFiltradas).toHaveLength(4);
  });

  it('pagina em 15, 25 ou 50 e volta à primeira página ao alterar os filtros', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c=fixture.componentInstance; const base=c.questoes()[0];
    c.questoes.set(Array.from({length:51},(_,i)=>({...base,id:i+1}))); fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr')).toHaveLength(15);
    c.pagina=2; expect(c.questoesDaPagina[0].id).toBe(16);
    c.changePageSize(25); expect(c.pagina).toBe(1); expect(c.questoesDaPagina).toHaveLength(25);
    c.changePageSize(50); c.pagina=2; expect(c.questoesDaPagina.map(q=>q.id)).toEqual([51]);
    c.ensinoAno='F:1'; c.changeAno(); expect(c.pagina).toBe(1);
  });

  it('alterna as abas e filtra pendências por ensino, ano e turma', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c=fixture.componentInstance;
    c.pendencias.set([{liberacaoId:1,titulo:'Prova',assuntoId:1,assunto:'Frações',materiaId:6,materia:'Matemática',turmaId:5,ensino:'F',ano:1,letra:'A',quantidades:[{peso:0.5,minimo:4,cadastradas:1,faltantes:3}]},
      {liberacaoId:1,titulo:'Prova',assuntoId:1,assunto:'Frações',materiaId:6,materia:'Matemática',turmaId:13,ensino:'F',ano:2,letra:'B',quantidades:[{peso:0.5,minimo:4,cadastradas:0,faltantes:4}]}]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.question-deficits')).toBeNull();
    (fixture.nativeElement.querySelector('#questoes-tab-faltantes') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('visual-pagination')).toBeNull();
    expect(fixture.nativeElement.querySelector('#questoes-tab-faltantes').getAttribute('aria-selected')).toBe('true');
    expect(fixture.nativeElement.querySelector('.question-deficits tbody tr td:last-child').textContent).toBe('3');
    expect(c.anosDisponiveis.some(a=>a.value==='F:2')).toBe(true);
    const select=fixture.nativeElement.querySelector('#questoes-filtro-ano') as HTMLSelectElement;
    select.value='F:1';select.dispatchEvent(new Event('change',{bubbles:true}));await fixture.whenStable();
    expect(c.pendenciasFiltradas).toHaveLength(1);
    expect(fixture.nativeElement.querySelectorAll('.question-deficits tbody tr')).toHaveLength(1);
    (fixture.nativeElement.querySelector('#questoes-tab-cadastradas') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('visual-pagination')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.question-deficits')).toBeNull();
  });

  it('não reabre o detalhe se a resposta chegar depois de recolher ou trocar de página', async () => {
    const fixture = TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c=fixture.componentInstance; let resolve!: (value: unknown) => void;
    get.mockReturnValueOnce(new Promise(r=>resolve=r));
    const pending=c.open(10); expect(c.expandedId()).toBe(10);
    c.changePage(2); resolve({id:10,tipo:'discursiva',peso:1,enunciado:'Texto',alternativas:{}}); await pending;
    expect(c.expandedId()).toBeNull(); expect(c.selecionada()).toBeNull();
  });

  it('não oferece turma sem vínculo mesmo que apareça em uma questão antiga', async () => {
    const fixture=TestBed.createComponent(ListarQuestoesComponent); await fixture.whenStable();
    const c=fixture.componentInstance;
    c.destinos.set([{materiaId:6,materia:'Matemática',turmaId:5,ensino:'F',ano:1,letra:'A'}]);
    c.questoes.set([{assuntoId:1,assunto:'Frações',id:10,materia:'Matemática',ensino:'F',ano:1,letras:'B',abrangencia:'turmas',tipo:'discursiva',peso:1,resumo:'Antiga'}]);
    c.ensinoAno='F:1';expect(c.turmasDisponiveis).toEqual(['A']);
  });

  it('filtra por peso decimal digitado com vírgula e restaura a lista ao limpar',async()=>{
    const fixture=TestBed.createComponent(ListarQuestoesComponent);await fixture.whenStable();
    const c=fixture.componentInstance,base=c.questoes()[0];c.questoes.set([{...base,id:1,peso:1.5},{...base,id:2,peso:2}]);fixture.detectChanges();
    const input=fixture.nativeElement.querySelector('#questoes-filtro-peso') as HTMLInputElement;
    input.value='1,5';input.dispatchEvent(new Event('input',{bubbles:true}));await fixture.whenStable();fixture.detectChanges();
    expect(c.peso).toBe(1.5);expect(c.questoesFiltradas.map(q=>q.id)).toEqual([1]);
    input.value='';input.dispatchEvent(new Event('input',{bubbles:true}));await fixture.whenStable();
    expect(c.peso).toBeNull();expect(c.questoesFiltradas).toHaveLength(2);
  });
});


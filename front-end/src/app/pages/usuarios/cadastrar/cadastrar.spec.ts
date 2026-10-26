import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { vi } from 'vitest';
import { ApiService } from '../../../services/api.service';
import { CadastrarUsuarioComponent } from './cadastrar';

describe('Cadastro de administrador', () => {
  const post = vi.fn();
  const get = vi.fn();
  const navigateByUrl = vi.fn();
  it('troca de perfil limpa credenciais e mostra a disponibilidade correta', async () => {
    const fixture = TestBed.createComponent(CadastrarUsuarioComponent);
    await fixture.whenStable();
    fixture.componentInstance.login = 'rascunho';
    fixture.componentInstance.senha = 'senha-teste-123';
    fixture.detectChanges();
    const select = fixture.nativeElement.querySelector('select') as HTMLSelectElement;
    select.value = 'professor'; select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(fixture.componentInstance.perfil).toBe('professor');
    expect(fixture.componentInstance.login).toBe('');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.nativeElement.querySelector('input[type="password"]')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#professor-nome')).toBeTruthy();
    expect(get).toHaveBeenCalledWith('/turmas');
    expect(post).not.toHaveBeenCalled();
    select.value = 'administrador'; select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('input[type="password"]')).toBeTruthy();
  });
  beforeEach(async () => {
    post.mockReset(); get.mockReset(); get.mockImplementation(async (path: string) => path === '/materias' ? [{ id: 10, nome: 'Matemática' }, { id: 20, nome: 'História' }] : [{ id: 1, ensino: 'F', ano: 1, letra: 'A' }, { id: 2, ensino: 'M', ano: 2, letra: 'B' }]);
    navigateByUrl.mockReset(); navigateByUrl.mockResolvedValue(true);
    await TestBed.configureTestingModule({ imports: [CadastrarUsuarioComponent], providers: [
      { provide: ApiService, useValue: { post, get } },
      { provide: Router, useValue: { navigateByUrl } },
    ] }).compileComponents();
  });

  async function submit(continuar = false) {
    const fixture = TestBed.createComponent(CadastrarUsuarioComponent);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    for (const [id, value] of [['administrador-login', 'novo.admin'], ['administrador-senha', 'senha-teste-123']]) {
      const input = element.querySelector('#' + id) as HTMLInputElement;
      input.value = value; input.dispatchEvent(new Event('input'));
    }
    await fixture.whenStable();
    if (continuar) { (element.querySelector('[variant=continue]') as HTMLButtonElement).click(); }
    else { element.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })); }
    await fixture.whenStable();
    return fixture;
  }

  it('cadastra o administrador e limpa os campos', async () => {
    post.mockResolvedValue({ id: 2 });
    const fixture = await submit();
    expect(post).toHaveBeenCalledWith('/administradores', { login: 'novo.admin', senha: 'senha-teste-123' });
    expect(navigateByUrl).toHaveBeenCalledWith('/cadastro/usuarios/listar');
    expect(fixture.componentInstance.login).toBe('');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.nativeElement.querySelector('[role="status"]').textContent).toContain('sucesso');
  });

  it('exibe login duplicado e apaga a senha sem informar sucesso', async () => {
    post.mockRejectedValue(new HttpErrorResponse({ status: 409, error: { mensagem: 'Este login já está cadastrado.' } }));
    const fixture = await submit();
    expect(fixture.componentInstance.erro()).toContain('já está cadastrado');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.componentInstance.mensagem()).toBe('');
  });
  it('cadastrar e continuar mantém o formulário pronto para outro registro', async () => {
    post.mockResolvedValue({ id: 3 });
    const fixture = await submit(true);
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(post).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.login).toBe('');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.componentInstance.busy()).toBe(false);
  });
  it('limpar apaga campos e mensagens sem enviar cadastro', async () => {
    const fixture = TestBed.createComponent(CadastrarUsuarioComponent);
    await fixture.whenStable();
    fixture.componentInstance.login = 'rascunho';
    fixture.componentInstance.senha = 'senha-teste-123';
    fixture.componentInstance.erro.set('Erro anterior');
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('[variant=secondary]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(post).not.toHaveBeenCalled();
    expect(fixture.componentInstance.login).toBe('');
    expect(fixture.componentInstance.senha).toBe('');
    expect(fixture.componentInstance.erro()).toBe('');
  });

  async function professorForm() {
    const fixture = TestBed.createComponent(CadastrarUsuarioComponent);
    await fixture.whenStable();
    const select = fixture.nativeElement.querySelector('#usuario-perfil') as HTMLSelectElement;
    select.value = 'professor'; select.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    for (const [id, value] of [['professor-nome', 'Maria Silva'], ['administrador-login', 'maria.prof'], ['administrador-senha', 'senha-teste-123']]) {
      const input = fixture.nativeElement.querySelector('#' + id) as HTMLInputElement;
      input.value = value; input.dispatchEvent(new Event('input'));
    }
    await fixture.whenStable();
    return fixture;
  }

  it('cadastra professor ativo com várias turmas e abre a lista de professores', async () => {
    post.mockResolvedValue({ id: 3 });
    const fixture = await professorForm();
    async function selectChip(id: string, query: string) {
      const input = fixture.nativeElement.querySelector('#' + id) as HTMLInputElement;
      input.value = query; input.dispatchEvent(new Event('input'));
      await fixture.whenStable();
      input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
      await fixture.whenStable();
    }
    await selectChip('professor-materias', 'matematica');
    await selectChip('professor-materias', 'historia');
    await selectChip('professor-turmas-10', 'fundamental');
    await selectChip('professor-turmas-10', 'medio');
    await selectChip('professor-turmas-20', 'medio');
    await fixture.whenStable();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/professores', { nome: 'Maria Silva', login: 'maria.prof', senha: 'senha-teste-123', materias: [{ materiaId: 10, turmas: [1, 2] }, { materiaId: 20, turmas: [2] }] });
    expect(navigateByUrl).toHaveBeenCalledWith('/cadastro/usuarios/listar?perfil=professor');
    expect(fixture.componentInstance.nome).toBe('');
    expect(fixture.componentInstance.materias).toEqual([]);
  });

  it('cadastra sem informar status e permite continuar com os campos limpos', async () => {
    post.mockResolvedValue({ id: 4 });
    const fixture = await professorForm();
    expect(fixture.nativeElement.querySelector('#professor-status')).toBeNull();
    const input = fixture.nativeElement.querySelector('#professor-materias') as HTMLInputElement;
    input.value = 'matematica'; input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('[variant=continue]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/professores', { nome: 'Maria Silva', login: 'maria.prof', senha: 'senha-teste-123', materias: [{ materiaId: 10, turmas: [] }] });
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(fixture.componentInstance.perfil).toBe('professor');
    expect(fixture.componentInstance.senha).toBe('');
  });

  it('explica a senha curta e libera o cadastro após corrigir para oito caracteres', async () => {
    post.mockResolvedValue({ id: 5 });
    const fixture = await professorForm();
    const materia = fixture.nativeElement.querySelector('#professor-materias') as HTMLInputElement;
    materia.value = 'matematica'; materia.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    materia.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true }));
    const senha = fixture.nativeElement.querySelector('#administrador-senha') as HTMLInputElement;
    senha.value = '123'; senha.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('#administrador-senha-erro').textContent).toContain('pelo menos 8 caracteres');
    expect(senha.getAttribute('aria-invalid')).toBe('true');
    expect(post).not.toHaveBeenCalled();
    senha.value = 'teste123'; senha.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#administrador-senha-erro')).toBeNull();
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(false);
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/professores', expect.objectContaining({ senha: 'teste123', materias: [{ materiaId: 10, turmas: [] }] }));
  });

  it('valida o login útil e senha em branco do administrador junto aos campos', async () => {
    const fixture = TestBed.createComponent(CadastrarUsuarioComponent); await fixture.whenStable();
    const login = fixture.nativeElement.querySelector('#administrador-login') as HTMLInputElement;
    const senha = fixture.nativeElement.querySelector('#administrador-senha') as HTMLInputElement;
    login.value = ' a '; login.dispatchEvent(new Event('input'));
    senha.value = '        '; senha.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#administrador-login-erro').textContent).toContain('3 caracteres');
    expect(fixture.nativeElement.querySelector('#administrador-senha-erro').textContent).toContain('apenas espaços');
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    expect(post).not.toHaveBeenCalled();
  });

  it('mostra erro nas matérias quando o professor remove a última seleção', async () => {
    const fixture = await professorForm();
    const materia = fixture.nativeElement.querySelector('#professor-materias') as HTMLInputElement;
    materia.value = 'matematica'; materia.dispatchEvent(new Event('input')); await fixture.whenStable();
    materia.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', cancelable: true })); await fixture.whenStable();
    (fixture.nativeElement.querySelector('.chip button') as HTMLButtonElement).click(); await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#professor-materias-erro').textContent).toContain('obrigatório');
    expect(materia.getAttribute('aria-invalid')).toBe('true');
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
  });

  it('falha no carregamento de turmas bloqueia o cadastro até tentar novamente', async () => {
    get.mockRejectedValueOnce(new Error('indisponível'));
    const fixture = await professorForm();
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    expect(fixture.componentInstance.erro()).toContain('carregar as matérias e turmas');
    (fixture.nativeElement.querySelector('fieldset button') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#professor-materias')).toBeTruthy();
    expect(post).not.toHaveBeenCalled();
  });
  it('exige matéria e descarta as turmas ao remover e selecionar a matéria novamente', async () => {
    const fixture = await professorForm();
    expect((fixture.nativeElement.querySelector('[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    fixture.componentInstance.changeMaterias([10, 20]);
    fixture.componentInstance.turmasPorMateria = { 10: [1], 20: [2] };
    fixture.componentInstance.changeMaterias([20]);
    fixture.componentInstance.changeMaterias([10, 20]);
    expect(fixture.componentInstance.turmasPorMateria).toEqual({ 10: [], 20: [2] });
  });
  async function alunoForm(numero='') {
    const fixture=TestBed.createComponent(CadastrarUsuarioComponent);await fixture.whenStable();
    const perfil=fixture.nativeElement.querySelector('#usuario-perfil') as HTMLSelectElement;
    perfil.value='aluno';perfil.dispatchEvent(new Event('change'));await fixture.whenStable();
    for(const [id,value] of [['aluno-nome','Aluno exemplo'],['aluno-cgm','001234567'],['aluno-numero',numero]]){
      const input=fixture.nativeElement.querySelector('#'+id) as HTMLInputElement;input.value=value;input.dispatchEvent(new Event('input'));
    }
    const turma=fixture.nativeElement.querySelector('#aluno-turma') as HTMLSelectElement;turma.selectedIndex=1;turma.dispatchEvent(new Event('change'));
    await fixture.whenStable();return fixture;
  }
  it('cadastra aluno sem número e abre a lista administrativa de alunos',async()=>{
    post.mockResolvedValue({id:1});const fixture=await alunoForm();
    expect(fixture.nativeElement.querySelector('input[type=password]')).toBeNull();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/alunos',{nome:'Aluno exemplo',numero:null,cgm:'001234567',status:'M',turmaId:1});
    expect(navigateByUrl).toHaveBeenCalledWith('/cadastro/usuarios/listar?perfil=aluno');expect(fixture.componentInstance.cgm).toBe('');
  });
  it('cadastra e continua com status transferido, limpando o formulário',async()=>{
    post.mockResolvedValue({id:1});const fixture=await alunoForm('12');
    const status=fixture.nativeElement.querySelector('#aluno-status') as HTMLSelectElement;status.value='T';status.dispatchEvent(new Event('change'));await fixture.whenStable();
    fixture.nativeElement.querySelector('[variant=continue]').click();await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/alunos',expect.objectContaining({numero:12,status:'T'}));expect(navigateByUrl).not.toHaveBeenCalled();
    expect(fixture.componentInstance.numero).toBeNull();expect(fixture.componentInstance.statusAluno).toBe('M');expect(fixture.componentInstance.turmaId).toBeNull();
  });
  it('não envia número fracionário no cadastro de aluno',async()=>{
    const fixture=await alunoForm('1.5');fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));await fixture.whenStable();
    expect(post).not.toHaveBeenCalled();
  });

});

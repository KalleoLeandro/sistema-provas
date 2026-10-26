import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { CadastrarTurmaComponent } from './cadastrar';
import { By } from '@angular/platform-browser';
import { NgForm } from '@angular/forms';

describe('Cadastro de turmas', () => {
  const post = vi.fn();
  beforeEach(() => {
    post.mockReset().mockResolvedValue({ id: 1, ensino: 'F', ano: 1, letra: 'A' });
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: ApiService, useValue: { post } }],
    });
  });
  async function setup() {
    const fixture = TestBed.createComponent(CadastrarTurmaComponent);
    await fixture.whenStable();
    return fixture;
  }
  async function fill(fixture: Awaited<ReturnType<typeof setup>>) {
    for (const [id, index] of [
      ['turma-ensino', 1],
      ['turma-ano', 1],
      ['turma-letra', 1],
    ] as const) {
      const select = fixture.nativeElement.querySelector('#' + id) as HTMLSelectElement;
      select.selectedIndex = index;
      select.dispatchEvent(new Event('change', { bubbles: true }));
    }
    await fixture.whenStable();
  }
  it('exige os três campos e oferece todos os anos e letras', async () => {
    const fixture = await setup();
    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('#turma-ano').options.length).toBe(10);
    expect(fixture.nativeElement.querySelector('#turma-letra').options.length).toBe(27);
    expect(post).not.toHaveBeenCalled();
  });
  it('cadastra com ano numérico e retorna à lista', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = await setup();
    await fill(fixture);
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(post).toHaveBeenCalledWith('/turmas', { ensino: 'F', ano: 1, letra: 'A' });
    expect(navigate).toHaveBeenCalledWith('/cadastro/turmas/listar');
    expect(fixture.componentInstance.ensino).toBe('');
  });
  it('cadastrar e continuar limpa os campos e mantém a página', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    const fixture = await setup();
    await fill(fixture);
    fixture.nativeElement.querySelector('[variant=continue]').click();
    await fixture.whenStable();
    expect(post).toHaveBeenCalledOnce();
    expect(navigate).not.toHaveBeenCalled();
    expect(fixture.componentInstance.ano).toBeNull();
    expect(fixture.componentInstance.letra).toBe('');
    expect(fixture.nativeElement.textContent).toContain('Turma cadastrada com sucesso.');
  });
  it('limpar descarta a seleção sem gravar', async () => {
    const fixture = await setup();
    await fill(fixture);
    fixture.nativeElement.querySelector('[variant=secondary]').click();
    await fixture.whenStable();
    expect(post).not.toHaveBeenCalled();
    expect(fixture.componentInstance.ensino).toBe('');
    expect(fixture.componentInstance.ano).toBeNull();
  });
  it('mostra os obrigatórios e impede ano ou ensino fora das opções previstas', async () => {
    const fixture = await setup();
    for (const id of ['turma-ensino', 'turma-ano', 'turma-letra']) {
      (fixture.nativeElement.querySelector('#' + id) as HTMLSelectElement).dispatchEvent(new Event('blur'));
    }
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#turma-ensino-erro').textContent).toContain('obrigatório');
    expect(fixture.nativeElement.querySelector('#turma-ano-erro').textContent).toContain('obrigatório');
    expect(fixture.nativeElement.querySelector('#turma-letra-erro').textContent).toContain('obrigatório');
    await fill(fixture);
    const form = fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm);
    form.controls['ano'].setValue(10); form.controls['ano'].markAsTouched();
    form.controls['ensino'].setValue('superior'); form.controls['ensino'].markAsTouched();
    fixture.changeDetectorRef.markForCheck(); await fixture.whenStable();
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#turma-ano-erro').textContent).toContain('opção válida');
    expect(fixture.nativeElement.querySelector('#turma-ensino-erro').textContent).toContain('opção válida');
    expect(post).not.toHaveBeenCalled();
  });

  it('preserva a seleção quando a API recusa duplicidade', async () => {
    post.mockRejectedValueOnce(
      new HttpErrorResponse({
        status: 409,
        error: { mensagem: 'Já existe uma turma com este ensino, ano e letra.' },
      }),
    );
    const fixture = await setup();
    await fill(fixture);
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.ano).toBe(1);
    expect(fixture.componentInstance.letra).toBe('A');
    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Já existe');
  });
});

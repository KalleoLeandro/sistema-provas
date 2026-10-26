import { TestBed } from '@angular/core/testing';
import { SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import Quill from 'quill';
import './root-radicand';

describe('Formatação das raízes', () => {
  let element: HTMLDivElement;
  let editor: Quill;
  beforeEach(() => {
    element = document.createElement('div');
    document.body.append(element);
    editor = new Quill(element, { modules: { toolbar: false } });
  });
  afterEach(() => element.remove());

  it('preserva o radicando ao sanitizar e reabrir o HTML salvo', () => {
    editor.setText('√1234 + 56');
    editor.formatText(1, 4, 'radicand', true);
    const html = editor.getSemanticHTML();
    const sanitized = TestBed.inject(DomSanitizer).sanitize(SecurityContext.HTML, html)!;
    editor.setContents(editor.clipboard.convert({ html: sanitized }));
    expect(element.querySelector('.raiz-radicando')?.textContent).toBe('1234');
    expect(editor.getFormat(1, 4)['radicand']).toBe(true);
    expect(editor.getFormat(5, 5)['radicand']).toBeUndefined();
  });

  it('mantém o índice separado da barra ao reabrir a raiz enésima', () => {
    editor.setContents(editor.clipboard.convert({ html: '<p><sup>5</sup>√<span class="raiz-radicando">32</span> = 2</p>' }));
    expect(element.querySelector('sup')?.textContent).toBe('5');
    expect(element.querySelector('.raiz-radicando')?.textContent).toBe('32');
    expect(editor.getSemanticHTML()).toContain('raiz-radicando');
  });
});

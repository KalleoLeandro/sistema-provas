import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import Quill from 'quill';
import { RichTextEditorComponent } from './rich-text-editor';

describe('Ajustes de figuras no editor',()=>{
 const html='<p><img src="data:image/png;base64,iVBORw0KGgo=" width="300" height="200" alt="Figura anterior"></p>';
 function setup(){
  const fixture=TestBed.createComponent(RichTextEditorComponent);
  fixture.componentRef.setInput('allowImages',true);fixture.detectChanges();
  const component=fixture.componentInstance;component.writeValue(html);
  const editor=Quill.find(fixture.nativeElement.querySelector('.ql-container')) as Quill;
  const change=vi.fn();component.registerOnChange(change);
  const image=fixture.nativeElement.querySelector('.ql-editor img') as HTMLImageElement;
  image.click();fixture.detectChanges();return {fixture,component,editor,change};
 }
 it('seleciona a figura e grava largura e descrição preservadas ao reabrir',()=>{
  const {component,editor,change,fixture}=setup();expect(fixture.nativeElement.textContent).toContain('Editar figura');
  component.imageWidth='180';component.imageAlt='Gráfico de vendas';component.applyImage();
  const saved=editor.getSemanticHTML();expect(saved).toContain('width="180"');expect(saved).toContain('alt="Gráfico de vendas"');expect(saved).not.toContain('height=');expect(change).toHaveBeenCalledWith(saved);
  component.writeValue(saved);expect(fixture.nativeElement.querySelector('.ql-editor img').getAttribute('width')).toBe('180');expect(component.selectedImage).toBeNull();
 });
 it('rejeita tamanho inválido sem alterar o HTML',()=>{
  const {component,editor,change}=setup();const before=editor.getSemanticHTML();component.imageWidth='-3';component.applyImage();expect(component.imageError).toContain('largura');expect(editor.getSemanticHTML()).toBe(before);expect(change).not.toHaveBeenCalled();
 });
 it('restaura o tamanho e permite desfazer remoção',()=>{
  const {component,editor,fixture}=setup();component.applyImage(true);expect(editor.getSemanticHTML()).not.toContain('width=');
  component.removeImage();expect(fixture.nativeElement.querySelector('.ql-editor img')).toBeNull();editor.history.undo();expect(fixture.nativeElement.querySelector('.ql-editor img')).not.toBeNull();
 });
 it('não altera figuras quando o formulário está desabilitado',()=>{
  const {component,editor}=setup();const before=editor.getSemanticHTML();component.setDisabledState(true);component.imageWidth='100';component.applyImage();component.removeImage();expect(editor.getSemanticHTML()).toBe(before);expect(component.selectedImage).toBeNull();
 });
 it('substitui a figura selecionada conservando largura e descrição',async()=>{
  const {component,editor,fixture}=setup();
  component.replaceImage();
  component.insertLocalImage({target:{files:[new File(['nova figura'],'nova.png',{type:'image/png'})],value:''}} as unknown as Event);
  await vi.waitFor(()=>expect(editor.getSemanticHTML()).toContain('bm92YSBmaWd1cmE='));
  expect(fixture.nativeElement.querySelectorAll('.ql-editor img')).toHaveLength(1);
  expect(editor.getSemanticHTML()).toContain('width="300"');expect(editor.getSemanticHTML()).toContain('alt="Figura anterior"');
  editor.history.undo();expect(editor.getSemanticHTML()).toContain('iVBORw0KGgo=');
 });
 it('redimensiona por arraste e desfaz todo o gesto de uma vez',()=>{
  const {component,editor,fixture}=setup();const image=component.selectedImage!;
  vi.spyOn(image,'getBoundingClientRect').mockImplementation(()=>({left:0,top:0,width:Number(image.getAttribute('width')),height:Number(image.getAttribute('width'))/1.5} as DOMRect));
  component.updateImageBox();fixture.detectChanges();
  const handle=fixture.nativeElement.querySelector('.rich-image-handle.se');
  handle.dispatchEvent(new PointerEvent('pointerdown',{pointerId:1,button:0,clientX:300,clientY:200,bubbles:true}));
  window.dispatchEvent(new PointerEvent('pointermove',{pointerId:1,clientX:360,clientY:240}));
  window.dispatchEvent(new PointerEvent('pointermove',{pointerId:1,clientX:420,clientY:280}));
  window.dispatchEvent(new PointerEvent('pointerup',{pointerId:1}));
  expect(image.getAttribute('width')).toBe('420');expect(editor.getSemanticHTML()).not.toContain('rich-image-handle');
  editor.history.undo();expect(fixture.nativeElement.querySelector('.ql-editor img').getAttribute('width')).toBe('300');
 });
 it('encerra o arraste ao limpar o editor sem reaplicar alterações',()=>{
  const {component,fixture,editor}=setup();
  fixture.nativeElement.querySelector('.rich-image-handle.se').dispatchEvent(new PointerEvent('pointerdown',{pointerId:2,button:0,bubbles:true}));
  component.writeValue('<p>Outra questão</p>');
  const saved=editor.getSemanticHTML();
  window.dispatchEvent(new PointerEvent('pointermove',{pointerId:2,clientX:500,clientY:500}));
  expect(editor.getText()).toContain('Outra questão');expect(component.imageBox).toBeNull();expect(editor.getSemanticHTML()).toBe(saved);
 });
});

describe('Símbolos científicos no editor',()=>{
 it('troca a categoria e insere símbolos de cálculo e química com opção de desfazer',()=>{
  Object.defineProperty(Range.prototype,'getBoundingClientRect',{configurable:true,value:()=>({left:0,top:0,right:0,bottom:0,width:0,height:0})});
  const fixture=TestBed.createComponent(RichTextEditorComponent);fixture.detectChanges();
  const component=fixture.componentInstance;
  const editor=Quill.find(fixture.nativeElement.querySelector('.ql-container')) as Quill;
  const change=vi.fn();component.registerOnChange(change);
  (fixture.nativeElement.querySelector('.rich-symbol-toggle') as HTMLButtonElement).click();fixture.detectChanges();
  const category=(label:string)=>Array.from(fixture.nativeElement.querySelectorAll('.rich-symbol-categories button') as NodeListOf<HTMLButtonElement>).find(b=>b.textContent?.trim()===label)!;
  category('Cálculo').click();fixture.detectChanges();
  (fixture.nativeElement.querySelector('button[aria-label="Integral dupla"]') as HTMLButtonElement).click();
  expect(editor.getText()).toContain('∬');expect(change).toHaveBeenCalled();
  category('Química').click();fixture.detectChanges();
  (fixture.nativeElement.querySelector('button[aria-label="Equilíbrio químico"]') as HTMLButtonElement).click();
  expect(editor.getText()).toContain('⇌');editor.history.undo();expect(editor.getText()).not.toContain('⇌');expect(editor.getText()).toContain('∬');
 });
});

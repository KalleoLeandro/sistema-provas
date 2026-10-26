import { TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Component } from '@angular/core';
import { TagFieldComponent } from './tag-field';
import { MarkdownComponent } from './markdown';
import { InfiniteScrollComponent } from './infinite-scroll';

@Component({
  imports: [FormsModule, TagFieldComponent],
  template: '<visual-tag-field [(ngModel)]="tags" [disabled]="disabled" />',
})
class TagForm {
  tags = ['Português'];
  disabled = false;
}

describe('Design system estendido', () => {
  it('sincroniza tags com o formulário e impede duplicatas', async () => {
    const fixture = TestBed.createComponent(TagForm);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    input.value = 'Matemática';
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.tags).toEqual(['Português', 'Matemática']);
    input.value = 'Matemática';
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    await fixture.whenStable();
    expect(fixture.componentInstance.tags).toHaveLength(2);
    fixture.nativeElement.querySelector('button').click();
    await fixture.whenStable();
    expect(fixture.componentInstance.tags).toEqual(['Matemática']);
  });
  it('desabilita edição e remoção de tags pelo formulário', async () => {
    const fixture = TestBed.createComponent(TagForm);
    fixture.componentInstance.disabled = true;
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('input').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('button').disabled).toBe(true);
  });
  it('sanitiza HTML inseguro vindo do Markdown', async () => {
    const fixture = TestBed.createComponent(MarkdownComponent);
    fixture.componentRef.setInput(
      'source',
      '<img src="x" onerror="alert(1)"><script>alert(1)</script> **Conteúdo**',
    );
    await fixture.whenStable();
    const article = fixture.nativeElement.querySelector('article') as HTMLElement;
    expect(article.querySelector('script')).toBeNull();
    expect(article.querySelector('img')?.getAttribute('onerror')).toBeNull();
    expect(article.textContent).toContain('Conteúdo');
  });
  it('não solicita mais registros durante carregamento ou após a última página', () => {
    const fixture = TestBed.createComponent(InfiniteScrollComponent);
    const emit = vi.spyOn(fixture.componentInstance.loadMore, 'emit');
    fixture.componentInstance.loading = true;
    fixture.componentInstance.requestMore();
    fixture.componentInstance.loading = false;
    fixture.componentInstance.hasMore = false;
    fixture.componentInstance.requestMore();
    expect(emit).not.toHaveBeenCalled();
    fixture.componentInstance.hasMore = true;
    fixture.componentInstance.requestMore();
    expect(emit).toHaveBeenCalledOnce();
  });
});

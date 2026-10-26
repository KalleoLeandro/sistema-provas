import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { RichTextEditorComponent } from '../extensions/rich-text-editor';
@Component({
  selector: 'ds-editor-example',
  imports: [FormsModule, RichTextEditorComponent],
  template:
    '<visual-rich-text-editor [allowImages]="true" [(ngModel)]="editorValue" /><details><summary>Conteúdo produzido</summary><pre>{{editorValue}}</pre></details>',
})
class EditorExampleComponent {
  editorValue = '<p>Calcule: x<sup>2</sup> + √(9) e log<sub>2</sub>(8). Use o painel de símbolos para inserir expressões.</p>';
}
const meta = {
  id: "componentes-rich-text-editor", title: "Componentes/Editor de texto formatado",
  component: EditorExampleComponent,
} satisfies Meta<EditorExampleComponent>;
export default meta;
export const Default: StoryObj<EditorExampleComponent> = { name: 'Padrão' };

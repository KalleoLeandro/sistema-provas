import { Component, Input } from '@angular/core';
import { QuestaoRascunho, LetraAlternativa } from '../models/questao';

@Component({
  selector: 'visual-question-preview',
  templateUrl: './question-preview.html',
  styleUrl: './question-preview.css',
})
export class QuestionPreviewComponent {
  @Input({ required: true }) questao!: QuestaoRascunho;
  readonly letras: LetraAlternativa[] = ['A', 'B', 'C', 'D', 'E'];
}

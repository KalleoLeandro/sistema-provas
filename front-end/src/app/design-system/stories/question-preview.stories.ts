import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { QuestionPreviewComponent } from '../question-preview';

export default { title: 'Design system/Prévia de questão', component: QuestionPreviewComponent, tags: ['autodocs'] } satisfies Meta<QuestionPreviewComponent>;
export const Objetiva: StoryObj<QuestionPreviewComponent> = {
  name: 'Objetiva',
  args: { questao: { id: 1, tipo: 'objetiva', peso: 1, enunciado: '<p>Quanto é <strong>2 + 2</strong>?</p>',
    alternativas: { A: '<p>2</p>', B: '<p>3</p>', C: '<p>4</p>', D: '<p>5</p>', E: '<p>6</p>' }, respostaCorreta: 'C' } },
};
export const Discursiva: StoryObj<QuestionPreviewComponent> = {
  name: 'Discursiva',
  args: { questao: { id: 2, tipo: 'discursiva', peso: 2, enunciado: '<p>Explique como você chegou à resposta.</p>',
    alternativas: { A: '', B: '', C: '', D: '', E: '' }, respostaCorreta: 'A' } },
};

import { type Meta, type StoryObj } from '@storybook/angular-vite';
import { AccordionComponent } from '../accordion';

export default { title: 'Design system/Accordion', component: AccordionComponent, tags: ['autodocs'] } satisfies Meta<AccordionComponent>;
export const Padrao: StoryObj<AccordionComponent> = {
  name: 'Padrão', render: () => ({ template: '<visual-accordion title="Questão 1 — Quanto é 2 + 2?" description="Matemática — Fundamental — 1º ano A"><p>Conteúdo da questão cadastrada.</p></visual-accordion>' }),
};

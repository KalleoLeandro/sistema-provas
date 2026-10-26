import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { NzCarouselModule } from '../library';
const meta = {
  id: "componentes-carousel", title: "Componentes/Carrossel",
  parameters: {
    docs: {
      description: { component: 'Componente reutilizável com a identidade visual do ESFINORA.' },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, NzCarouselModule],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{ slides: ['Avaliações', 'Conhecimento', 'Aprendizagem'] } },
    template:
      '<nz-carousel nzEffect="scrollx" [nzAutoPlay]="false">@for (item of slides; track item) {<div nz-carousel-content><div class="ds-carousel-slide">{{item}}</div></div>}</nz-carousel>',
  }),
};

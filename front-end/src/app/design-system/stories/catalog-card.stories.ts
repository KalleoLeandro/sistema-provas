import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { CardComponent } from '../components';
const meta = {
  id: "componentes-card", title: "Componentes/Cartão",
  decorators: [moduleMetadata({ imports: [CardComponent] })],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    template:
      '<visual-cartao><h2>Matérias</h2><p>Organize os conteúdos e avaliações.</p></visual-cartao>',
  }),
};

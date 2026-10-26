import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { ButtonDirective, InputDirective, FieldComponent } from '../components';
import { PdfViewerComponent } from '../library';
const meta = {
  id: "componentes-pdf-viewer", title: "Componentes/Visualizador de PDF",
  parameters: {
    docs: {
      description: {
        component:
          'Visualizador nativo do navegador com link alternativo. Aceita arquivos locais ou PDFs do próprio portal.',
      },
    },
  },
  decorators: [
    moduleMetadata({
      imports: [FormsModule, ButtonDirective, InputDirective, FieldComponent, PdfViewerComponent],
    }),
  ],
} satisfies Meta;
export default meta;
export const Default: StoryObj = {
  name: 'Padrão',
  render: () => ({
    props: { ...{} },
    template: '<visual-pdf-viewer src="storybook/exemplo.pdf" />',
  }),
};

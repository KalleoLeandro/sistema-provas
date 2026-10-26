import { FormsModule } from '@angular/forms';
import { moduleMetadata, type Meta, type StoryObj } from '@storybook/angular-vite';
import { FieldComponent, InputDirective } from '../components';
import { NotBlankDirective, ValidationMessagePipe } from '../validators';

export default {
  title: 'Design system/Validação de formulários',
  decorators: [moduleMetadata({ imports: [FormsModule, FieldComponent, InputDirective, NotBlankDirective, ValidationMessagePipe] })],
  tags: ['autodocs'],
} satisfies Meta;

export const CampoObrigatorio: StoryObj = {
  name: 'Campo obrigatório',
  render: () => ({ template: `<form #form="ngForm" autocomplete="off">
    <visual-campo rotulo="Nome da matéria" identificador="exemplo-validacao" orientacao="Até 120 caracteres." [erro]="nome | validationMessage:form.submitted">
      <input campoVisual id="exemplo-validacao" name="nome" ngModel #nome="ngModel" required notBlank maxlength="120"
        aria-describedby="exemplo-validacao-erro" [attr.aria-invalid]="(nome | validationMessage:form.submitted) ? 'true' : null" />
    </visual-campo></form>` }),
};

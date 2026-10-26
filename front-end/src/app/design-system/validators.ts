import { Directive, forwardRef, Input, Pipe, PipeTransform } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, NgControl, ValidationErrors, Validator } from '@angular/forms';

@Directive({
  selector: '[richTextRequired][ngModel]',
  providers: [{ provide: NG_VALIDATORS, useExisting: forwardRef(() => RichTextRequiredDirective), multi: true }],
})
export class RichTextRequiredDirective implements Validator {
  /** Considera texto ou imagem como conteúdo e rejeita a marcação vazia produzida pelo editor. */
  validate(control: AbstractControl): ValidationErrors | null {
    /* A análise usa um documento inerte; não executa nem renderiza o conteúdo recebido. */
    const html = typeof control.value === 'string' ? control.value : '';
    const document = new DOMParser().parseFromString(html, 'text/html');
    return document.body.textContent?.replace(/\u200b/g, '').trim() || document.querySelector('img[src]')
      ? null : { required: true };
  }
}

@Directive({
  selector: '[notBlank][ngModel]',
  providers: [{ provide: NG_VALIDATORS, useExisting: forwardRef(() => NotBlankDirective), multi: true }],
})
export class NotBlankDirective implements Validator {
  /** Rejeita texto composto somente por espaços; o vazio é tratado pelo required. */
  validate(control: AbstractControl): ValidationErrors | null {
    /* Preserva o conteúdo digitado e a senha; nenhuma normalização altera o valor do campo. */
    return typeof control.value === 'string' && control.value.length > 0 && !control.value.trim()
      ? { notBlank: true } : null;
  }
}

@Directive({
  selector: '[trimmedMinlength][ngModel]',
  providers: [{ provide: NG_VALIDATORS, useExisting: forwardRef(() => TrimmedMinlengthDirective), multi: true }],
})
export class TrimmedMinlengthDirective implements Validator {
  @Input({ required: true }) trimmedMinlength = 0;

  /** Confere o comprimento útil do login, como o back-end após remover espaços externos. */
  validate(control: AbstractControl): ValidationErrors | null {
    /* Uma cadeia vazia continua sob responsabilidade do required. */
    if (typeof control.value !== 'string' || !control.value.length) return null;
    const actualLength = control.value.trim().length;
    return actualLength < this.trimmedMinlength
      ? { minlength: { requiredLength: this.trimmedMinlength, actualLength } } : null;
  }
}

@Directive({
  selector: '[allowedValues][ngModel]',
  providers: [{ provide: NG_VALIDATORS, useExisting: forwardRef(() => AllowedValuesDirective), multi: true }],
})
export class AllowedValuesDirective implements Validator {
  @Input({ required: true }) allowedValues: readonly unknown[] = [];

  /** Restringe seleções aos valores previstos, incluindo o tipo numérico do ano. */
  validate(control: AbstractControl): ValidationErrors | null {
    /* Ausência de seleção gera required, sem duplicar mensagens. */
    if (control.value === null || control.value === undefined || control.value === '') return null;
    return this.allowedValues.includes(control.value) ? null : { allowedValues: true };
  }
}

@Pipe({ name: 'validationMessage', pure: false })
export class ValidationMessagePipe implements PipeTransform {
  /** Exibe a primeira mensagem do campo somente após interação ou tentativa de envio. */
  transform(control: NgControl | null | undefined, submitted = false): string {
    /* O estado do NgControl muda sem trocar sua referência; a mensagem acompanha cada verificação da tela. */
    if (!control || (!control.dirty && !control.touched && !submitted) || !control.errors) return '';
    const errors = control.errors;
    if (errors['required']) return 'Este campo é obrigatório.';
    if (errors['notBlank']) return 'Este campo não pode conter apenas espaços.';
    if (errors['minlength']) return `Informe pelo menos ${errors['minlength'].requiredLength} caracteres.`;
    if (errors['maxlength']) return `Informe no máximo ${errors['maxlength'].requiredLength} caracteres.`;
    if (errors['allowedValues']) return 'Selecione uma opção válida.';
    if (errors['pattern']) return 'O formato informado é inválido.';
    return 'Verifique o valor informado.';
  }
}

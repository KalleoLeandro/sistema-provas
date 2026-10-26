import { FormControl, NgControl } from '@angular/forms';
import { AllowedValuesDirective, NotBlankDirective, RichTextRequiredDirective, TrimmedMinlengthDirective, ValidationMessagePipe } from './validators';

describe('Validadores dos cadastros', () => {
  it('rejeita HTML vazio do editor e aceita texto ou imagem', () => {
    const validator = new RichTextRequiredDirective();
    for (const html of ['', '<p><br></p>', '<p>&nbsp; </p>']) expect(validator.validate(new FormControl(html))).toEqual({ required: true });
    expect(validator.validate(new FormControl('<p>Enunciado</p>'))).toBeNull();
    expect(validator.validate(new FormControl('<img src="data:image/png;base64,aA==" />'))).toBeNull();
  });
  it('rejeita espaços sem modificar o valor da senha', () => {
    const validator = new NotBlankDirective();
    const control = new FormControl('        ');
    expect(validator.validate(control)).toEqual({ notBlank: true });
    expect(control.value).toBe('        ');
    expect(validator.validate(new FormControl(' senha '))).toBeNull();
    expect(validator.validate(new FormControl(''))).toBeNull();
  });
  it('confere o login sem contar espaços externos', () => {
    const validator = new TrimmedMinlengthDirective(); validator.trimmedMinlength = 3;
    expect(validator.validate(new FormControl(' a '))?.['minlength'].actualLength).toBe(1);
    expect(validator.validate(new FormControl(' abc '))).toBeNull();
  });
  it('restringe o ano a inteiros de 1 a 9 e o ensino aos códigos válidos', () => {
    const validator = new AllowedValuesDirective();
    validator.allowedValues = Array.from({ length: 9 }, (_, index) => index + 1);
    for (const value of [0, 10, 1.5, '1']) expect(validator.validate(new FormControl(value))).toEqual({ allowedValues: true });
    expect(validator.validate(new FormControl(9))).toBeNull();
    validator.allowedValues = ['F', 'M'];
    expect(validator.validate(new FormControl('fundamental'))).toEqual({ allowedValues: true });
    expect(validator.validate(new FormControl('F'))).toBeNull();
  });
  it('exibe mensagens somente após interação ou envio e prioriza o obrigatório', () => {
    const pipe = new ValidationMessagePipe();
    const control = { dirty: false, touched: false, errors: { required: true, notBlank: true } } as unknown as NgControl;
    expect(pipe.transform(control)).toBe('');
    expect(pipe.transform(control, true)).toBe('Este campo é obrigatório.');
    expect(pipe.transform({ dirty: true, errors: { notBlank: true } } as unknown as NgControl)).toContain('apenas espaços');
  });
});

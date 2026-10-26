package br.com.provas.validation;
import br.com.provas.model.request.LoginRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
public class LoginRequestValidator implements ConstraintValidator<ValidLogin,LoginRequest> {
    @Override public boolean isValid(LoginRequest request,ConstraintValidatorContext context) {
        if(request==null)return true;
        boolean valid=true;
        context.disableDefaultConstraintViolation();
        if("aluno".equals(request.perfil())) {
            if(request.login()!=null && !request.login().matches("[0-9]{1,30}")) {
                context.buildConstraintViolationWithTemplate("Informe um CGM de até 30 dígitos.").addPropertyNode("login").addConstraintViolation();valid=false;
            }
        } else {
            if(request.login()!=null && request.login().length()<3) {
                context.buildConstraintViolationWithTemplate("O login deve conter de 3 a 120 caracteres.").addPropertyNode("login").addConstraintViolation();valid=false;
            }
            if(request.senha()==null || request.senha().isBlank() || request.senha().length()<8) {
                context.buildConstraintViolationWithTemplate("Informe uma senha de 8 a 128 caracteres.").addPropertyNode("senha").addConstraintViolation();valid=false;
            }
        }
        return valid;
    }
}

package br.com.provas.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=LoginRequestValidator.class)
public @interface ValidLogin {
    String message() default "Revise os dados de acesso.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

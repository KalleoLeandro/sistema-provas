package br.com.provas.validation;
import br.com.provas.model.request.AlunoRequest;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AlunoRequestValidationTest {
    @Test void missingFieldsAreIdentifiedAndNumberIsOptional(){
        try(var factory=Validation.buildDefaultValidatorFactory()){
            var validator=factory.getValidator();
            var errors=validator.validate(new AlunoRequest(null,null,null,null,null));
            var fields=errors.stream().map(e->e.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
            assertTrue(fields.containsAll(Set.of("nome","cgm","status","turmaId")));assertFalse(fields.contains("numero"));
            var valid=new AlunoRequest(" Ana ",null,"001234","m",5L);
            assertTrue(validator.validate(valid).isEmpty());assertEquals("001234",valid.cgm());assertEquals("M",valid.status());assertEquals("Ana",valid.nome());
        }
    }
    @Test void rejectsFractionalNumberInvalidStatusAndNonNumericCgm(){
        try(var factory=Validation.buildDefaultValidatorFactory()){
            var fields=factory.getValidator().validate(new AlunoRequest("Ana",new BigDecimal("1.5"),"12A","Ativo",5L)).stream()
                .map(e->e.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
            assertTrue(fields.containsAll(Set.of("numero","cgm","status")));
        }
    }
}

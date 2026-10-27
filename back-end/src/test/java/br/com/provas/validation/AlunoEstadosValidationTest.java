package br.com.provas.validation;
import br.com.provas.model.request.AlunoRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AlunoEstadosValidationTest {
 @Test void acceptsOnlyTheFourSchoolStates(){try(var factory=Validation.buildDefaultValidatorFactory()){
  for(String status:java.util.List.of("M","T","R","D"))assertTrue(factory.getValidator().validate(new AlunoRequest("Ana",null,"001",status,1L)).isEmpty());
  assertFalse(factory.getValidator().validate(new AlunoRequest("Ana",null,"001","X",1L)).isEmpty());
 }}
}

package br.com.provas.validation;

import br.com.provas.model.request.TurmaRequest;
import jakarta.validation.Validation;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TurmaRequestValidationTest {
    @Test
    void acceptsAllYearsAndBoundaryLettersForBothEducationTypes() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var validator=factory.getValidator();
            for(var ensino:new String[]{"F","M"}) for(int ano=1;ano<=9;ano++) for(var letra:new String[]{"A","Z"})
                assertTrue(validator.validate(new TurmaRequest(ensino,ano,letra)).isEmpty());
        }
    }
    @Test
    void reportsEveryMissingField() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var fields=factory.getValidator().validate(new TurmaRequest(null,null,null)).stream()
                .map(error->error.getPropertyPath().toString()).collect(Collectors.toSet());
            assertEquals(Set.of("ensino","ano","letra"),fields);
        }
    }
    @Test
    void rejectsUnknownEducationOutOfRangeYearsAndNonAlphabeticLetters() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var validator=factory.getValidator();
            for(var request:new TurmaRequest[]{new TurmaRequest("superior",1,"A"),new TurmaRequest("M",0,"A"),new TurmaRequest("M",10,"A"),new TurmaRequest("M",1,"AB"),new TurmaRequest("M",1,"1"),new TurmaRequest("M",1,"Á")})
                assertFalse(validator.validate(request).isEmpty());
        }
    }
}

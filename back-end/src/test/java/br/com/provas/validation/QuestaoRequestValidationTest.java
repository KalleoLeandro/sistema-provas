package br.com.provas.validation;

import br.com.provas.model.request.QuestaoRequest;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestaoRequestValidationTest {
    @Test void missingFieldsAreIdentified() {
        try(var factory = Validation.buildDefaultValidatorFactory()) {
            var campos = factory.getValidator().validate(new QuestaoRequest(null,null,null,null,null,null,null,null,null,null, 1L)).stream()
                .map(v -> v.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
            assertTrue(campos.containsAll(java.util.List.of("materiaId","ensino","ano","abrangencia","turmas","tipo","peso","enunciado","alternativas")));
        }
    }
    @Test void invalidIdentifiersWeightAndAlternativeKeysAreRejected() {
        try(var factory = Validation.buildDefaultValidatorFactory()) {
            var erros = factory.getValidator().validate(new QuestaoRequest(0L,"Z",0,"invalida",java.util.List.of(-1L),"invalida",new BigDecimal("0"),"Texto",Map.of("Z","Texto"),"Z", 1L));
            assertTrue(erros.size() >= 6);
        }
    }
}

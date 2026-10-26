package br.com.provas.validation;

import br.com.provas.model.request.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfessorRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Test void missingFieldsAreReported() {
        var fields = validator.validate(new ProfessorRequest(null, null, null, null)).stream()
            .map(v -> v.getPropertyPath().toString()).collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of("nome", "login", "senha", "materias"), fields);
    }
    @Test void rejectsMissingMaterias() {
        assertFalse(validator.validate(new ProfessorRequest("Maria", "maria", "senha-teste-123", List.of())).isEmpty());
    }
    @Test void nestedFieldsAreValidated() {
        var violations = validator.validate(new ProfessorRequest("Maria", "maria", "senha-teste-123",
            List.of(new ProfessorMateriaRequest(null, Arrays.asList(0L, null)))));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("materiaId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("turmas[0]")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("turmas[1]")));
    }
    @Test void allowsMateriaWithoutTurmas() {
        assertTrue(validator.validate(new ProfessorRequest("Maria", "maria", "senha-teste-123",
            List.of(new ProfessorMateriaRequest(1L, List.of())))).isEmpty());
    }
}

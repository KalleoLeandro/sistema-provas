package br.com.provas.validation;

import br.com.provas.model.request.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestValidationTest {
    @Test
    void allRequestsDeclareRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (Object request : new Object[] { new LoginRequest(null, null), new UsuarioAdministrativoRequest(null, null),
                new AtualizarUsuarioAdministrativoRequest(null, null), new MateriaRequest(null), new StatusRequest(null) }) {
                assertFalse(validator.validate(request).isEmpty(), request.getClass().getSimpleName());
            }
            assertTrue(validator.validate(new StatusRequest(false)).isEmpty());
        }
    }

    @Test
    void optionalPasswordPreservesExistingContractAndRejectsInvalidValues() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String password : new String[] { null, "", "senha-valida-123" })
                assertTrue(validator.validate(new AtualizarUsuarioAdministrativoRequest("admin", password)).isEmpty());
            for (String password : new String[] { "curta", " ".repeat(8), "a".repeat(129) })
                assertTrue(validator.validate(new AtualizarUsuarioAdministrativoRequest("admin", password)).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("senha")));
        }
    }

    @Test
    void nameAndLoginLengthsAreValidatedAfterTrimming() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new MateriaRequest("  " + "a".repeat(120) + "  ")).isEmpty());
            assertFalse(validator.validate(new MateriaRequest("a".repeat(121))).isEmpty());
            assertFalse(validator.validate(new UsuarioAdministrativoRequest("  ab  ", "senha-valida-123")).isEmpty());
        }
    }
    @Test void studentCgmDoesNotRequirePasswordButStaffStillDoes(){
        try(var factory=Validation.buildDefaultValidatorFactory()){
            var validator=factory.getValidator();
            assertTrue(validator.validate(new LoginRequest("00123",null,"aluno")).isEmpty());
            assertFalse(validator.validate(new LoginRequest("ABC",null,"aluno")).isEmpty());
            assertTrue(validator.validate(new LoginRequest("prof",null,"professor")).stream().anyMatch(e->e.getPropertyPath().toString().equals("senha")));
        }
    }
}

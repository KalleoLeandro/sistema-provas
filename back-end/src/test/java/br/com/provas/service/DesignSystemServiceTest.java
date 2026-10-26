package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DesignSystemServiceTest {
    private final UsuarioAdministrativoRepository repository = mock(UsuarioAdministrativoRepository.class);
    private final DesignSystemService service = new DesignSystemService(repository);

    @Test
    void onlyActiveMasterIsAllowed() {
        when(repository.isActiveMaster(1L)).thenReturn(true);
        assertDoesNotThrow(() -> service.authorize(new LoginResponse(1L, "Master User", "administrador", "administrador")));
        verify(repository).isActiveMaster(1L);
    }

    @Test
    void nameAndLoginDoNotGrantMasterPermission() {
        when(repository.isActiveMaster(2L)).thenReturn(false);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(DefaultErrorException.class,
            () -> service.authorize(new LoginResponse(2L, "Master User", "administrador", "administrador"))).getStatus());
    }

    @Test
    void otherProfilesCannotReuseMasterId() {
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(DefaultErrorException.class,
            () -> service.authorize(new LoginResponse(1L, "Professor", "professor", "professor"))).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void missingSessionReturnsUnauthorized() {
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(DefaultErrorException.class,
            () -> service.authorize(null)).getStatus());
        verifyNoInteractions(repository);
    }
}

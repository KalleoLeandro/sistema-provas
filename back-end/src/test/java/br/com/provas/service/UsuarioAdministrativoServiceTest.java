package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.UsuarioAdministrativo;
import br.com.provas.model.request.UsuarioAdministrativoRequest;
import br.com.provas.model.request.StatusRequest;
import br.com.provas.model.request.AtualizarUsuarioAdministrativoRequest;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioAdministrativoServiceTest {
    private final UsuarioAdministrativoRepository repository = mock(UsuarioAdministrativoRepository.class);
    private final Pbkdf2PasswordEncoder encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private final UsuarioAdministrativoService service = new UsuarioAdministrativoService(repository, encoder);

    @Test
    void editingWithoutNewPasswordKeepsHashAndStatus() {
        when(repository.findById(2L)).thenReturn(Optional.of(new UsuarioAdministrativo(2L, "Admin", "antigo", "hash-original", false)));
        when(repository.findByLogin("novo")).thenReturn(Optional.empty());
        var response = service.update(2L, new AtualizarUsuarioAdministrativoRequest("novo", null));
        verify(repository).update(2L, "novo", "hash-original");
        assertFalse(response.ativo());
    }

    @Test
    void editingRejectsLoginOwnedByAnotherUser() {
        when(repository.findById(2L)).thenReturn(Optional.of(new UsuarioAdministrativo(2L, "Admin", "antigo", "hash", true)));
        when(repository.findByLogin("ocupado")).thenReturn(Optional.of(new UsuarioAdministrativo(3L, "Outro", "ocupado", "hash", true)));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class,
            () -> service.update(2L, new AtualizarUsuarioAdministrativoRequest("ocupado", null))).getStatus());
        verify(repository, never()).update(anyLong(), anyString(), anyString());
    }

    @Test
    void updatesStatusWithoutChangingCredentials() {
        var usuario = new UsuarioAdministrativo(2L, "Admin", "admin", "hash", true);
        when(repository.findById(2L)).thenReturn(Optional.of(usuario));
        var response = service.updateStatus(2L, new StatusRequest(false));
        assertFalse(response.ativo());
        assertEquals("admin", response.login());
        verify(repository).updateStatus(2L, false);
        verify(repository, never()).save(anyString(), anyString());
    }

    @Test
    void missingStatusDoesNotDeactivateAccount() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(DefaultErrorException.class,
            () -> service.updateStatus(2L, new StatusRequest(null))).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void missingAccountReturnsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(DefaultErrorException.class,
            () -> service.updateStatus(99L, new StatusRequest(false))).getStatus());
        verify(repository, never()).updateStatus(anyLong(), anyBoolean());
    }

    @Test
    void savesHashInsteadOfRawPassword() {
        when(repository.findByLogin("novo.admin")).thenReturn(Optional.empty());
        when(repository.save(eq("novo.admin"), anyString())).thenAnswer(invocation -> {
            String hash = invocation.getArgument(1);
            assertNotEquals("senha-teste-123", hash);
            assertTrue(encoder.matches("senha-teste-123", hash));
            return new UsuarioAdministrativo(2L, "novo.admin", "novo.admin", hash, true);
        });
        var response = service.create(new UsuarioAdministrativoRequest(" novo.admin ", "senha-teste-123"));
        assertEquals("novo.admin", response.login());
        assertEquals(2L, response.id());
    }

    @Test
    void rejectsInvalidFieldsBeforeWriting() {
        for (var request : new UsuarioAdministrativoRequest[] {
            new UsuarioAdministrativoRequest("  ", "senha-teste-123"),
            new UsuarioAdministrativoRequest("admin", "curta"),
            new UsuarioAdministrativoRequest("admin", " ".repeat(10)),
            new UsuarioAdministrativoRequest("admin", "a".repeat(129))
        }) {
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows(DefaultErrorException.class,
                () -> service.create(request)).getStatus());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsExistingLogin() {
        when(repository.findByLogin("admin")).thenReturn(Optional.of(
            new UsuarioAdministrativo(1L, "Admin", "admin", "hash", true)));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class,
            () -> service.create(new UsuarioAdministrativoRequest("admin", "senha-teste-123"))).getStatus());
        verify(repository, never()).save(anyString(), anyString());
    }

    @Test
    void handlesConcurrentDuplicateCreation() {
        when(repository.findByLogin("admin")).thenReturn(Optional.empty());
        when(repository.save(eq("admin"), anyString())).thenThrow(new DuplicateKeyException("duplicate"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class,
            () -> service.create(new UsuarioAdministrativoRequest("admin", "senha-teste-123"))).getStatus());
    }
}

package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.*;
import br.com.provas.model.request.*;
import br.com.provas.repository.ProfessorRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfessorServiceTest {
    private final ProfessorRepository repository = mock(ProfessorRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final ProfessorService service = new ProfessorService(repository, encoder);
    private final List<ProfessorMateriaRequest> materias = List.of(new ProfessorMateriaRequest(10L, List.of(1L, 2L)), new ProfessorMateriaRequest(20L, List.of(2L)));
    private ProfessorRequest request(List<ProfessorMateriaRequest> items) { return new ProfessorRequest(" Maria Silva ", " maria.prof ", "senha-teste-123", items); }
    private Professor professor(boolean ativo) { return new Professor(3L, "Maria Silva", "maria.prof", "hash", ativo); }
    private void prepare() {
        when(repository.findExistingMateriaIds(List.of(10L, 20L))).thenReturn(List.of(10L, 20L));
        when(repository.findExistingTurmaIds(List.of(1L, 2L))).thenReturn(List.of(1L, 2L));
        when(encoder.encode(anyString())).thenReturn("hash");
        when(repository.save("Maria Silva", "maria.prof", "hash")).thenReturn(professor(true));
        when(repository.findMateriasByProfessorIds(List.of(3L))).thenReturn(Map.of(3L, List.of(
            new ProfessorMateria(10L, "Matemática", List.of(new Turma(1L, "F", 1, "A"), new Turma(2L, "M", 2, "B"))),
            new ProfessorMateria(20L, "História", List.of(new Turma(2L, "M", 2, "B"))))));
    }
    @Test void savesSpecificTurmasForEachMateriaAndHashesPassword() {
        prepare(); var result = service.create(request(materias));
        assertEquals(2, result.materias().size()); assertEquals(2, result.materias().getFirst().turmas().size());
        assertEquals(1, result.materias().get(1).turmas().size()); assertEquals(2, result.turmas().size());
        verify(repository).saveMaterias(3L, materias, true); verify(encoder).encode("senha-teste-123");
        assertFalse(result.toString().contains("senha-teste-123"));
    }
    @Test void duplicateMateriaIsRejectedBeforeSaving() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(DefaultErrorException.class,
            () -> service.create(request(List.of(materias.getFirst(), materias.getFirst())))).getStatus());
        verifyNoInteractions(repository, encoder);
    }
    @Test void duplicateTurmaWithinMateriaIsRejected() {
        assertThrows(DefaultErrorException.class, () -> service.create(request(List.of(new ProfessorMateriaRequest(10L, List.of(1L, 1L))))));
        verifyNoInteractions(repository, encoder);
    }
    @Test void missingMateriaIsRejectedBeforeSaving() {
        when(repository.findExistingMateriaIds(List.of(10L, 20L))).thenReturn(List.of(10L));
        assertThrows(DefaultErrorException.class, () -> service.create(request(materias))); verifyNoInteractions(encoder);
    }
    @Test void missingTurmaIsRejectedBeforeSaving() {
        when(repository.findExistingMateriaIds(List.of(10L, 20L))).thenReturn(List.of(10L, 20L));
        when(repository.findExistingTurmaIds(List.of(1L, 2L))).thenReturn(List.of(1L));
        assertThrows(DefaultErrorException.class, () -> service.create(request(materias))); verifyNoInteractions(encoder);
    }
    @Test void duplicateLoginReturnsConflict() {
        prepare(); when(repository.save(anyString(), anyString(), anyString())).thenThrow(new DuplicateKeyException("login"));
        assertTrue(assertThrows(DefaultErrorException.class, () -> service.create(request(materias))).getMessage().contains("login"));
    }
    @Test void activeAssignmentCollisionReturnsConflict() {
        prepare(); doThrow(new DuplicateKeyException("assignment")).when(repository).saveMaterias(3L, materias, true);
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class, () -> service.create(request(materias))).getStatus());
    }
    @Test void activationCollisionDoesNotUpdateProfessorStatus() {
        when(repository.findByIdForUpdate(3L)).thenReturn(Optional.of(professor(false)));
        doThrow(new DuplicateKeyException("assignment")).when(repository).updateMateriasStatus(3L, true);
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class, () -> service.updateStatus(3L, new StatusRequest(true))).getStatus());
        verify(repository, never()).updateStatus(anyLong(), anyBoolean());
    }
    @Test void deactivationReleasesAssignmentsAndPreservesMaterias() {
        prepare(); when(repository.findByIdForUpdate(3L)).thenReturn(Optional.of(professor(true)));
        when(repository.findTurmasByProfessorIds(List.of(3L))).thenReturn(Map.of());
        var result = service.updateStatus(3L, new StatusRequest(false));
        assertFalse(result.ativo()); assertEquals(2, result.materias().size());
        verify(repository).updateMateriasStatus(3L, false); verify(repository).updateStatus(3L, false);
    }
}

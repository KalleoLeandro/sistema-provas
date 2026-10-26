package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Materia;
import br.com.provas.model.request.MateriaRequest;
import br.com.provas.repository.MateriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MateriaServiceTest {
    private final MateriaRepository repository = mock(MateriaRepository.class);
    private final MateriaService service = new MateriaService(repository);

    @Test
    void savesTrimmedName() {
        when(repository.save("Matemática")).thenReturn(new Materia(1L, "Matemática"));
        assertEquals("Matemática", service.create(new MateriaRequest("  Matemática  ")).nome());
        verify(repository).save("Matemática");
    }

    @Test
    void rejectsEmptyOrOversizedNames() {
        for (var nome : new String[] { null, "", "   ", "a".repeat(121) }) {
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows(DefaultErrorException.class,
                () -> service.create(new MateriaRequest(nome))).getStatus());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void returnsConflictForDuplicateName() {
        when(repository.save("Matemática")).thenThrow(new DuplicateKeyException("duplicate"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class,
            () -> service.create(new MateriaRequest("Matemática"))).getStatus());
    }

    @Test
    void editingTrimsNameAndReportsDuplicates() {
        when(repository.update(1L, "Historia")).thenReturn(1);
        assertEquals("Historia", service.update(1L, new MateriaRequest("  Historia  ")).nome());
        when(repository.update(1L, "Ocupada")).thenThrow(new DuplicateKeyException("duplicate"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class,
            () -> service.update(1L, new MateriaRequest("Ocupada"))).getStatus());
    }

    @Test
    void editingRejectsBlankNameWithoutWriting() {
        assertThrows(DefaultErrorException.class, () -> service.update(1L, new MateriaRequest("  ")));
        verifyNoInteractions(repository);
    }

    @Test
    void missingRecordsReturnNotFoundForEditAndDelete() {
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(DefaultErrorException.class,
            () -> service.update(99L, new MateriaRequest("Historia"))).getStatus());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(DefaultErrorException.class,
            () -> service.delete(99L)).getStatus());
    }

    @Test
    void linkedSubjectCannotBeDeleted() {
        when(repository.delete(1L)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("constraint"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(DefaultErrorException.class, () -> service.delete(1L)).getStatus());
    }
}

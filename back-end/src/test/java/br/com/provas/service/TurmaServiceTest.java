package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Turma;
import br.com.provas.model.request.TurmaRequest;
import br.com.provas.repository.TurmaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TurmaServiceTest {
    private final TurmaRepository repository = mock(TurmaRepository.class);
    private final TurmaService service = new TurmaService(repository);

    @Test
    void createsWithNormalizedIdentity() {
        when(repository.save("M", 9, "Z")).thenReturn(new Turma(2L,"M",9,"Z"));
        var response = service.create(new TurmaRequest(" m ",9," z "));
        assertEquals("Z",response.letra()); assertEquals("M",response.ensino()); assertEquals(2L,response.id());
        verify(repository).save("M",9,"Z");
    }

    @Test
    void duplicateCombinationReturnsConflict() {
        when(repository.save("F",1,"A")).thenThrow(new DuplicateKeyException("duplicate"));
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,
            () -> service.create(new TurmaRequest("F",1,"A"))).getStatus());
    }
}

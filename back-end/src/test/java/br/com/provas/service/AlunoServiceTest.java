package br.com.provas.service;
import br.com.provas.repository.AlunoRepository;
import br.com.provas.exceptions.DefaultErrorException;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AlunoServiceTest {
    private final AlunoRepository repository=mock(AlunoRepository.class);
    private final AlunoService service=new AlunoService(repository);
    @Test void paginatesWithinTheAuthenticatedProfessorAndSelectedClass() {
        when(repository.countByProfessor(7L,5L)).thenReturn(60L);
        when(repository.findByProfessor(7L,5L,25,25L)).thenReturn(List.of());
        var page=service.findAll(7L,5L,2,25);
        assertEquals(60L,page.total()); assertEquals(2,page.pagina());
        verify(repository).findByProfessor(7L,5L,25,25L);
    }
    @Test void clampsMissingPagesAndRejectsUnsupportedSizes() {
        when(repository.countByProfessor(7L,null)).thenReturn(16L);
        when(repository.findByProfessor(7L,null,15,15L)).thenReturn(List.of());
        assertEquals(2,service.findAll(7L,null,99,15).pagina());
        assertThrows(DefaultErrorException.class,()->service.findAll(7L,null,1,20));
        assertThrows(DefaultErrorException.class,()->service.findAll(7L,null,0,15));
    }
    @Test void createsStudentWithOptionalNumberAndSchoolStatus(){
        var request=new br.com.provas.model.request.AlunoRequest("Ana",null,"001234","T",5L);
        when(repository.turmaExists(5L)).thenReturn(true);
        when(repository.save(request)).thenReturn(new br.com.provas.model.response.AlunoResponse(1L,"Ana",null,"001234","T",5L,"F",1,"A"));
        var response=service.create(request);assertNull(response.numero());assertEquals("T",response.status());assertEquals("001234",response.cgm());
    }
    @Test void refusesUnknownClassAndDuplicateCgm(){
        var request=new br.com.provas.model.request.AlunoRequest("Ana",null,"001234","M",5L);
        assertThrows(DefaultErrorException.class,()->service.create(request));verify(repository,never()).save(any());
        when(repository.turmaExists(5L)).thenReturn(true);
        when(repository.save(request)).thenThrow(new org.springframework.dao.DuplicateKeyException("duplicate"));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.create(request)).getStatus());
    }

}

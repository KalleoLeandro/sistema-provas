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

    @Test void editsStudentWithoutRecreatingIdentity(){
        var request=new br.com.provas.model.request.AlunoRequest("Ana",null,"001234","T",5L);when(repository.lockById(1L)).thenReturn(true);when(repository.turmaExists(5L)).thenReturn(true);when(repository.findById(1L)).thenReturn(java.util.Optional.of(new br.com.provas.model.response.AlunoResponse(1L,"Ana",null,"001234","T",5L,"F",1,"A")));
        var result=service.update(1L,request);assertEquals(1L,result.id());assertEquals("001234",result.cgm());verify(repository).update(1L,request);verify(repository,never()).save(any());
    }
    @Test void editsRejectMissingStudentAndUnknownClass(){
        var request=new br.com.provas.model.request.AlunoRequest("Ana",null,"001234","M",5L);assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,()->service.update(1L,request)).getStatus());when(repository.lockById(1L)).thenReturn(true);assertThrows(DefaultErrorException.class,()->service.update(1L,request));verify(repository,never()).update(any(),any());
    }
    @Test void editsRejectDuplicateCgm(){
        var request=new br.com.provas.model.request.AlunoRequest("Ana",null,"001234","M",5L);when(repository.lockById(1L)).thenReturn(true);when(repository.turmaExists(5L)).thenReturn(true);doThrow(new org.springframework.dao.DuplicateKeyException("cgm")).when(repository).update(1L,request);assertEquals(org.springframework.http.HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.update(1L,request)).getStatus());
    }
}

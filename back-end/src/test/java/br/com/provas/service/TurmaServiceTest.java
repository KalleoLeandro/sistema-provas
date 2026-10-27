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

    @Test void refusesDeletionWithPeopleRegardlessOfTheirStatus(){
        when(repository.findById(4L,true)).thenReturn(java.util.Optional.of(new Turma(4L,"F",1,"A")));
        when(repository.hasPeople(4L)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.delete(4L)).getStatus());
        verify(repository,never()).delete(any());
    }
    @Test void protectsExamAndQuestionReferences(){
        when(repository.findById(4L,true)).thenReturn(java.util.Optional.of(new Turma(4L,"F",1,"A")));
        doThrow(new org.springframework.dao.DataIntegrityViolationException("fk")).when(repository).delete(4L);
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.delete(4L)).getStatus());
    }
    @Test void deletesAnUnlinkedClassAndRejectsMissingClass(){
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,()->service.delete(4L)).getStatus());
        when(repository.findById(4L,true)).thenReturn(java.util.Optional.of(new Turma(4L,"F",1,"A")));
        service.delete(4L);verify(repository).delete(4L);
    }
    @Test void updatesWithoutChangingIdentityAndRejectsDuplicate(){
        when(repository.findById(4L,true)).thenReturn(java.util.Optional.of(new Turma(4L,"F",1,"A")));
        when(repository.findById(4L,false)).thenReturn(java.util.Optional.of(new Turma(4L,"F",1,"B")));
        var request=new TurmaRequest("F",1,"B");assertEquals(4L,service.update(4L,request).id());
        verify(repository).update(4L,request);verify(repository,never()).save(any(),any(),any());
        doThrow(new DuplicateKeyException("duplicate")).when(repository).update(4L,request);
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.update(4L,request)).getStatus());
    }
}

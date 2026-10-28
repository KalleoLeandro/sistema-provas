package br.com.provas.service;
import br.com.provas.repository.AssuntoRepository;
import br.com.provas.model.request.AssuntoRequest;
import br.com.provas.model.response.AssuntoResponse;
import br.com.provas.exceptions.DefaultErrorException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AssuntoServiceTest {
 private final AssuntoRepository repository=mock(AssuntoRepository.class);private final AssuntoService service=new AssuntoService(repository);
 @Test void deniesCatalogAndCreationWithoutTeachingAssignment(){assertThrows(DefaultErrorException.class,()->service.search(7L,6L,"fra"));assertThrows(DefaultErrorException.class,()->service.create(7L,new AssuntoRequest(6L,"Frações")));verify(repository,never()).search(any(),any());verify(repository,never()).save(any(),any());}
 @Test void normalizesNamesAndReturnsThePersistedSubject(){when(repository.allowed(7L,6L)).thenReturn(true);when(repository.save(6L,"Equações Lineares")).thenReturn(new AssuntoResponse(10L,6L,"Equações Lineares"));var result=service.create(7L,new AssuntoRequest(6L,"  Equações   Lineares  "));assertEquals(10L,result.id());verify(repository).save(6L,"Equações Lineares");}
}

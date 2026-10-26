package br.com.provas.service;
import br.com.provas.repository.ProvaAlunoRepository;
import br.com.provas.exceptions.DefaultErrorException;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProvaAlunoServiceTest {
    @Test void progressoDaEntregaUsaTotaisCompletosSemCarregarPagina(){
        var repository=mock(ProvaAlunoRepository.class);var now=Instant.parse("2030-01-01T12:00:00Z");
        var execution=mock(ExecucaoProvaService.class);var service=new ProvaAlunoService(repository,Clock.fixed(now,ZoneOffset.UTC),execution);
        when(repository.countAvailable(9L,now)).thenReturn(20L);when(repository.countCompleted(9L,now)).thenReturn(4L);
        var result=service.progresso(9L);assertEquals(20,result.total());assertEquals(4,result.concluidas());verifyNoInteractions(execution);
    }
    @Test void totalsDoNotDependOnPaginationAndUseServerTime(){
        var repository=mock(ProvaAlunoRepository.class);var now=Instant.parse("2030-01-01T12:00:00Z");
        var service=new ProvaAlunoService(repository,Clock.fixed(now,ZoneOffset.UTC),mock(ExecucaoProvaService.class));
        when(repository.countAvailable(9L,now)).thenReturn(20L);
        when(repository.findAvailable(9L,now,15,15L)).thenReturn(List.of());
        var result=service.findAvailable(9L,99,15);
        assertEquals(20,result.total());assertEquals(0,result.concluidas());assertEquals(2,result.provas().pagina());
        verify(repository).findAvailable(9L,now,15,15L);
        assertThrows(DefaultErrorException.class,()->service.findAvailable(9L,1,20));
    }
}

package br.com.provas.service;
import br.com.provas.repository.ProvaRepository;
import br.com.provas.model.entity.LiberacaoProva;
import br.com.provas.model.request.LiberarProvaRequest;
import br.com.provas.exceptions.DefaultErrorException;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProvaServiceTest {
    private final Instant now=Instant.parse("2030-01-01T12:00:00Z");
    private final ProvaRepository repository=mock(ProvaRepository.class);
    private final org.springframework.security.crypto.password.PasswordEncoder encoder=mock(org.springframework.security.crypto.password.PasswordEncoder.class);
    private final br.com.provas.security.TokenCipher cipher=mock(br.com.provas.security.TokenCipher.class);
    private final ProvaService service=new ProvaService(repository,Clock.fixed(now,ZoneOffset.UTC),encoder,cipher);
    @Test void derivesScheduledAvailableAndExpiredAtExactBoundaries(){
        when(repository.hasClass(7L,1L)).thenReturn(true);when(repository.count(7L,1L)).thenReturn(3L);
        when(repository.findAll(7L,1L,15,0L)).thenReturn(List.of(
            new LiberacaoProva(1L,"Agendada",6L,"Matemática",now.plusSeconds(1),now.plusSeconds(60)),
            new LiberacaoProva(2L,"Disponível",6L,"Matemática",now,now.plusSeconds(60)),
            new LiberacaoProva(3L,"Encerrada",6L,"Matemática",now.minusSeconds(60),now)));
        var page=service.findAll(7L,1L,1,15);
        assertEquals(List.of("agendada","disponivel","encerrada"),page.itens().stream().map(p->p.situacao()).toList());
    }
    @Test void refusesOtherStudentsAndInvalidPagination(){
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,()->service.findAll(7L,2L,1,15)).getStatus());
        verify(repository,never()).findAll(any(),any(),anyInt(),anyLong());
        when(repository.hasClass(7L,1L)).thenReturn(true);
        assertThrows(DefaultErrorException.class,()->service.findAll(7L,1L,1,20));
    }
    @Test void requiresFutureEndAndStrictlyIncreasingPeriod(){
        for(var request:List.of(new LiberarProvaRequest("Prova",6L,now,now),new LiberarProvaRequest("Prova",6L,now.minusSeconds(60),now))){
            assertThrows(DefaultErrorException.class,()->service.release(7L,1L,request));
        }
        verify(repository,never()).save(any(),any(),any(),any(),any());
    }
    @Test void rejectsInactiveTransferredOrUnrelatedSubjectAndSavesValidRelease(){
        var request=new LiberarProvaRequest("Prova",6L,now.minusSeconds(10),now.plusSeconds(60));
        when(repository.lockClassSubject(7L,1L,6L)).thenReturn(false);
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN,assertThrows(DefaultErrorException.class,()->service.release(7L,1L,request)).getStatus());
        verify(repository,never()).save(any(),any(),any(),any(),any());
        when(repository.lockClassSubject(7L,1L,6L)).thenReturn(true);
        when(repository.save(eq(7L),eq(1L),eq(request),nullable(String.class),nullable(String.class))).thenReturn(new LiberacaoProva(1L,"Prova",6L,"Matemática",request.disponivelEm(),request.encerraEm()));
        assertEquals("disponivel",service.release(7L,1L,request).situacao());
        verify(repository).save(eq(7L),eq(1L),eq(request),nullable(String.class),nullable(String.class));
    }
    @Test void studentConsultationResolvesClassInsteadOfIndividualRelease(){
        when(repository.findStudentClass(7L,9L)).thenReturn(5L);
        when(repository.hasClass(7L,5L)).thenReturn(true);
        when(repository.count(7L,5L)).thenReturn(0L);
        when(repository.findAll(7L,5L,15,0L)).thenReturn(List.of());
        service.findByStudent(7L,9L,1,15);
        verify(repository).findAll(7L,5L,15,0L);
        assertThrows(DefaultErrorException.class,()->service.findByStudent(7L,99L,1,15));
    }
    @Test void editReturnsCurrentFieldsAndDecryptsOnlyForOwner(){
        when(repository.hasClass(7L,5L)).thenReturn(true);
        var record=new LiberacaoProva(11L,"Título atual",6L,"Matemática",now,now.plusSeconds(3600),75,true,"ciphertext");
        when(repository.findById(7L,5L,11L)).thenReturn(java.util.Optional.of(record));when(cipher.decrypt("ciphertext")).thenReturn("TokenVisivel");
        var result=service.getForEdit(7L,5L,11L);assertEquals("Título atual",result.titulo());assertEquals(now,result.disponivelEm());assertEquals(75,result.duracaoMinutos());assertEquals("TokenVisivel",result.token());assertFalse(result.precisaNovoToken());
        assertThrows(DefaultErrorException.class,()->service.getForEdit(8L,5L,11L));
    }
}

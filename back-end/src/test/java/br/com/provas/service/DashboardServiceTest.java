package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.DashboardIndicadoresResponse;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.DashboardRepository;
import java.time.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
 private final DashboardRepository repository=mock(DashboardRepository.class);
 private final Instant now=Instant.parse("2026-10-25T12:00:00Z");
 private final DashboardService service=new DashboardService(repository,Clock.fixed(now,ZoneOffset.UTC));
 private final DashboardIndicadoresResponse resumo=new DashboardIndicadoresResponse(2,3,new BigDecimal("7.5"),3,1,1,2,List.of());
 @Test void professorUsaSomenteSeuIdEInstanteConsistente(){
  when(repository.resumo(21L,now)).thenReturn(resumo);when(repository.medias(21L,now)).thenReturn(List.of(new DashboardIndicadoresResponse.MediaTurma(5L,"F",1,"A",new BigDecimal("7.5"),2)));
  var result=service.indicadores(new LoginResponse(21L,"Professor","prof","professor"));
  assertEquals(2,result.alunosFizeram());assertEquals(3,result.alunosPendentes());assertEquals(new BigDecimal("7.5"),result.mediaGeral());assertEquals(5L,result.mediasTurmas().getFirst().turmaId());
  verify(repository).resumo(21L,now);verify(repository).medias(21L,now);
 }
 @Test void administradorAbrangeTodosESemCorrecoesNaoGeraZero(){
  when(repository.resumo(null,now)).thenReturn(new DashboardIndicadoresResponse(0,0,null,0,0,0,0,List.of()));when(repository.medias(null,now)).thenReturn(List.of());
  assertNull(service.indicadores(new LoginResponse(1L,"Admin","admin","administrador")).mediaGeral());verify(repository).medias(null,now);
 }
 @Test void alunoNaoConsultaIndicadores(){assertThrows(DefaultErrorException.class,()->service.indicadores(new LoginResponse(72L,"Ana","001","aluno")));verifyNoInteractions(repository);}
}

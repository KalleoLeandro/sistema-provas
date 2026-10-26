package br.com.provas.controller;

import br.com.provas.model.request.SubmeterProvaRequest;
import br.com.provas.model.response.*;
import br.com.provas.service.ExecucaoProvaService;
import br.com.provas.service.ProvaAlunoService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExecucaoProvaControllerTest {
 private final ExecucaoProvaService service=mock(ExecucaoProvaService.class);
 private final ProvaAlunoService provas=mock(ProvaAlunoService.class);
 private final ExecucaoProvaController controller=new ExecucaoProvaController(service,provas);
 private final LoginResponse aluno=new LoginResponse(9L,"Aluno","001","aluno");
 private TentativaProvaResponse response(boolean agora){var now=Instant.parse("2030-01-01T12:00:00Z");return new TentativaProvaResponse(5L,"Prova","Matemática",now,now,now,"submetida",agora,List.of());}
 @Test void entregaIncluiProgressoAntesDeEncerrarSessao(){
  var request=new MockHttpServletRequest();var session=(MockHttpSession)request.getSession();var body=new SubmeterProvaRequest(true);
  when(service.submit(9L,5L,body)).thenReturn(response(true));when(provas.progresso(9L)).thenReturn(new ProgressoProvasResponse(20,4));
  var result=controller.submit(aluno,5L,body,request);assertEquals(20,result.progresso().total());assertEquals(4,result.progresso().concluidas());assertTrue(session.isInvalid());
 }
 @Test void entregaAutomaticaTambemIncluiProgresso(){
  var request=new MockHttpServletRequest();var session=(MockHttpSession)request.getSession();when(service.get(9L,5L)).thenReturn(response(true));when(provas.progresso(9L)).thenReturn(new ProgressoProvasResponse(2,2));
  assertEquals(2,controller.get(aluno,5L,request).progresso().concluidas());assertTrue(session.isInvalid());
 }
 @Test void falhaNoResumoNaoDesfazEntregaNemMantemSessao(){
  var request=new MockHttpServletRequest();var session=(MockHttpSession)request.getSession();var body=new SubmeterProvaRequest(true);when(service.submit(9L,5L,body)).thenReturn(response(true));when(provas.progresso(9L)).thenThrow(new IllegalStateException("Consulta indisponível"));
  assertEquals("submetida",controller.submit(aluno,5L,body,request).situacao());assertTrue(session.isInvalid());
 }
 @Test void revisarEntregaAntigaNaoDeslogaNemConsultaResumo(){
  var request=new MockHttpServletRequest();var session=(MockHttpSession)request.getSession();when(service.get(9L,5L)).thenReturn(response(false));controller.get(aluno,5L,request);assertFalse(session.isInvalid());verifyNoInteractions(provas);
 }
}

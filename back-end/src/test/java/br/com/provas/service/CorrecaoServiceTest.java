package br.com.provas.service;
import br.com.provas.repository.CorrecaoRepository;
import br.com.provas.model.request.*;
import br.com.provas.model.response.*;
import br.com.provas.exceptions.DefaultErrorException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CorrecaoServiceTest {
 private final CorrecaoRepository repository=mock(CorrecaoRepository.class);private final Instant now=Instant.parse("2030-01-01T12:00:00Z");private final CorrecaoService service=new CorrecaoService(repository,Clock.fixed(now,ZoneOffset.UTC));
 private final ProvaCorrecaoResumoResponse header=new ProvaCorrecaoResumoResponse(1L,3L,"Prova","Matemática","Ana","001",5L,"F",1,"A",now,1,BigDecimal.ZERO);
 private final QuestaoCorrecaoResponse objetiva=new QuestaoCorrecaoResponse(1,"objetiva",new BigDecimal("0.5"),"Texto",Map.of("A","a"),"A","A",new BigDecimal("0.5"),null,0,null);
 private final QuestaoCorrecaoResponse discursiva=new QuestaoCorrecaoResponse(2,"discursiva",new BigDecimal("1.5"),"Texto",Map.of(),null,"Resposta",null,null,0,null);
 private SalvarCorrecaoProvaRequest request(String nota){return new SalvarCorrecaoProvaRequest(List.of(new SalvarCorrecaoProvaRequest.Item(1,new CorrigirQuestaoRequest(new BigDecimal("0.5"),"Feedback objetivo",0L)),new SalvarCorrecaoProvaRequest.Item(2,new CorrigirQuestaoRequest(new BigDecimal(nota),"Feedback discursivo",0L))));}
 private void setup(){when(repository.findById(7L,1L)).thenReturn(Optional.of(header));when(repository.questions(7L,1L)).thenReturn(List.of(objetiva,discursiva));}
 @Test void savesAllQuestionsInOneOperation(){setup();when(repository.update(eq(7L),eq(1L),anyInt(),any(),eq(now))).thenReturn(true);var request=request("1.25");service.save(7L,1L,request);verify(repository).update(7L,1L,1,request.questoes().get(0).correcao(),now);verify(repository).update(7L,1L,2,request.questoes().get(1).correcao(),now);}
 @Test void invalidLaterQuestionPreventsEveryWrite(){setup();assertThrows(DefaultErrorException.class,()->service.save(7L,1L,request("1.51")));verify(repository,never()).update(any(),any(),anyInt(),any(),any());}
 @Test void missingOrDuplicateQuestionsAreRejected(){setup();var items=request("1").questoes();assertThrows(DefaultErrorException.class,()->service.save(7L,1L,new SalvarCorrecaoProvaRequest(List.of(items.get(0)))));assertThrows(DefaultErrorException.class,()->service.save(7L,1L,new SalvarCorrecaoProvaRequest(List.of(items.get(0),items.get(0)))));verify(repository,never()).update(any(),any(),anyInt(),any(),any());}
 @Test void staleRevisionIsDetectedBeforeWritingOtherQuestions(){setup();var items=request("1").questoes();var stale=new SalvarCorrecaoProvaRequest.Item(2,new CorrigirQuestaoRequest(BigDecimal.ONE,"Comentário",1L));assertEquals(org.springframework.http.HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.save(7L,1L,new SalvarCorrecaoProvaRequest(List.of(items.get(0),stale)))).getStatus());verify(repository,never()).update(any(),any(),anyInt(),any(),any());}
 @Test void objectiveGradeAndBlankZeroArePreserved(){setup();var items=request("1").questoes();var wrong=new SalvarCorrecaoProvaRequest.Item(1,new CorrigirQuestaoRequest(BigDecimal.ZERO,null,0L));assertThrows(DefaultErrorException.class,()->service.save(7L,1L,new SalvarCorrecaoProvaRequest(List.of(wrong,items.get(1)))));var blank=new QuestaoCorrecaoResponse(2,"discursiva",new BigDecimal("1.5"),"Texto",Map.of(),null,null,BigDecimal.ZERO,null,0,null);when(repository.questions(7L,1L)).thenReturn(List.of(objetiva,blank));assertThrows(DefaultErrorException.class,()->service.save(7L,1L,request("0.5")));}
 @Test void otherTeacherIsRejected(){assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,()->service.get(8L,1L)).getStatus());}
 @Test void nestedValidationReportsInvalidGrade(){try(var factory=jakarta.validation.Validation.buildDefaultValidatorFactory()){var invalid=new SalvarCorrecaoProvaRequest(List.of(new SalvarCorrecaoProvaRequest.Item(1,new CorrigirQuestaoRequest(new BigDecimal("0.123"),null,0L))));assertFalse(factory.getValidator().validate(invalid).isEmpty());}}
 @Test void completedCorrectionCanBeRevised(){
  var completed=new ProvaCorrecaoResumoResponse(1L,3L,"Prova","Matemática","Ana","001",5L,"F",1,"A",now,0,new BigDecimal("1.5"));
  var graded=new QuestaoCorrecaoResponse(2,"discursiva",new BigDecimal("1.5"),"Texto",Map.of(),null,"Resposta",BigDecimal.ONE,"Feedback anterior",2,now.minusSeconds(60));
  when(repository.findById(7L,1L)).thenReturn(Optional.of(completed));when(repository.questions(7L,1L)).thenReturn(List.of(objetiva,graded));when(repository.update(eq(7L),eq(1L),anyInt(),any(),eq(now))).thenReturn(true);
  var updated=new CorrigirQuestaoRequest(new BigDecimal("0.75"),"Feedback revisado",2L);var items=List.of(request("1").questoes().get(0),new SalvarCorrecaoProvaRequest.Item(2,updated));service.save(7L,1L,new SalvarCorrecaoProvaRequest(items));verify(repository).update(7L,1L,2,updated,now);
 }
}

package br.com.provas.service;
import br.com.provas.model.entity.*;
import br.com.provas.model.request.*;
import br.com.provas.model.response.*;
import br.com.provas.repository.*;
import br.com.provas.exceptions.DefaultErrorException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ExecucaoProvaServiceTest {
 private final Instant now=Instant.parse("2030-01-01T12:00:00Z");
 private final ExecucaoProvaRepository repository=mock(ExecucaoProvaRepository.class);
 private final ProvaRedisRepository redis=mock(ProvaRedisRepository.class);
 private final EntregaProvaService delivery=mock(EntregaProvaService.class);
 private final PasswordEncoder encoder=mock(PasswordEncoder.class);
 private final ExecucaoProvaService service=new ExecucaoProvaService(repository,Clock.fixed(now,ZoneOffset.UTC),encoder,redis,delivery);
 private List<QuestaoSorteio> bank(){var bank=new ArrayList<QuestaoSorteio>();long id=0;for(String peso:List.of("0.5","1.0","1.5","2.0"))for(int i=0;i<10;i++)bank.add(new QuestaoSorteio(++id,id,"objetiva",new BigDecimal(peso),"Texto",Map.of("A","a","B","b"),"A"));return bank;}
 private EstadoProvaRedis state(boolean sealed,boolean persisted,Instant end,String answer,long revision){
  var copy=new TentativaProvaResponse(5L,"Prova","Matemática",now.minusSeconds(60),end,now,"em_andamento",false,List.of(new QuestaoExecucaoResponse(1,"objetiva",new BigDecimal("0.5"),"Texto",Map.of("A","a","B","b"),null,0)));
  return new EstadoProvaRedis(1L,copy,Map.of(1,answer),Map.of(1,revision),sealed,persisted,sealed?now:null,false);
 }
 @Test void drawAlwaysRespectsCompositionAndOrderWithoutRepeating(){for(int i=0;i<500;i++){var result=service.draw(bank());assertEquals(10,result.size());assertEquals(10,result.stream().map(QuestaoSorteio::id).distinct().count());assertEquals(0,result.getFirst().peso().compareTo(new BigDecimal("0.5")));for(int n=0;n<6;n++)assertTrue(result.get(n).peso().compareTo(new BigDecimal("2.0"))<0);int[] counts={4,3,2,1};for(int n=0;n<4;n++){final int w=n+1;assertEquals(counts[n],result.stream().filter(q->q.peso().multiply(BigDecimal.valueOf(2)).intValue()==w).count());}}}
 @Test void missingWeightDoesNotProducePartialExam(){assertThrows(DefaultErrorException.class,()->service.draw(bank().stream().filter(q->q.peso().doubleValue()!=2).toList()));}
 @Test void answerUsesRedisWithoutAnyMysqlOperation(){
  when(redis.find(9L,5L)).thenReturn(Optional.of(state(false,false,now.plusSeconds(100),"",0)),Optional.of(state(false,false,now.plusSeconds(100),"A",1)));
  when(redis.answer(9L,5L,1,"A",0)).thenReturn(1L);
  var response=service.save(9L,5L,1,new SalvarRespostaRequest("A",0L));assertEquals("A",response.questoes().getFirst().resposta());verifyNoInteractions(repository,delivery);
 }
 @Test void staleRevisionCannotOverwriteAnswer(){
  when(redis.find(9L,5L)).thenReturn(Optional.of(state(false,false,now.plusSeconds(100),"B",1)));when(redis.answer(9L,5L,1,"A",0)).thenReturn(-1L);
  assertThrows(DefaultErrorException.class,()->service.save(9L,5L,1,new SalvarRespostaRequest("A",0L)));verifyNoInteractions(repository,delivery);
 }
 @Test void incompleteDeliveryRequiresExplicitConfirmation(){
  when(redis.find(9L,5L)).thenReturn(Optional.of(state(false,false,now.plusSeconds(100),"",0)));when(redis.seal(9L,5L,false,false)).thenReturn(-4L);
  assertThrows(DefaultErrorException.class,()->service.submit(9L,5L,new SubmeterProvaRequest(false)));verifyNoInteractions(delivery);
 }
 @Test void sealedDeliveryCanBeRetriedWithoutLosingRedisAnswers(){
  var sealed=state(true,false,now,"A",2);when(redis.find(9L,5L)).thenReturn(Optional.of(sealed));when(delivery.persist(9L,5L,sealed)).thenThrow(new IllegalStateException("SQL indisponível")).thenReturn(true);
  assertThrows(IllegalStateException.class,()->service.get(9L,5L));verify(redis,never()).finished(any(),any());
  var response=service.get(9L,5L);assertEquals("submetida",response.situacao());assertTrue(response.enviadaAgora());verify(redis).finished(9L,5L);
 }
 @Test void repeatedStartNeverDrawsAgain(){
  when(redis.lockStart(9L,5L)).thenReturn("lease");when(repository.findAttempt(9L,5L)).thenReturn(Optional.of(new TentativaProva(1L,5L,9L,"Prova","Matemática",now.minusSeconds(60),now.plusSeconds(60),null)));
  when(redis.find(9L,5L)).thenReturn(Optional.of(state(false,false,now.plusSeconds(60),"A",1)));
  assertEquals("em_andamento",service.start(9L,5L,new IniciarProvaRequest("Token",true)).situacao());verify(repository,never()).candidates(any());verify(redis).unlockStart(9L,5L,"lease");
 }
}

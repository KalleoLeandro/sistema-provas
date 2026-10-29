package br.com.provas.service;
import br.com.provas.model.request.*;import br.com.provas.model.entity.QuestaoSorteio;import br.com.provas.exceptions.DefaultErrorException;
import org.junit.jupiter.api.Test;import java.util.*;import java.math.BigDecimal;import static org.junit.jupiter.api.Assertions.*;
class ProofPlanningServiceTest {
 @Test void requirementAppliesOnlyToTheCheckedSubject() {
  var config=new ConfiguracaoProvaRequest(new BigDecimal("5"),4,List.of(
    new ConfiguracaoProvaRequest.Assunto(1L,List.of(BigDecimal.ONE,new BigDecimal("2")),true),
    a(2,"1","3")));
  var pool=List.of(q(1,BigDecimal.ONE),q(1,new BigDecimal("2")),q(2,BigDecimal.ONE),q(2,BigDecimal.ONE));
  var result=ProofPlanningService.analyze(config,pool,Map.of());
  assertTrue(result.possivel());
  assertEquals(2,result.composicao().stream().filter(i->i.assuntoId()==1L).count());
  assertEquals(1,result.composicao().stream().filter(i->i.assuntoId()==2L).count());
 }
 @Test void oldJsonDefaultsOffAndNewJsonPreservesTheOption() {
  var mapper=new tools.jackson.databind.ObjectMapper();
  var old=mapper.readValue("{\"valorTotal\":4,\"quantidadeQuestoes\":3,\"assuntos\":[{\"assuntoId\":1,\"pesos\":[1,2]}]}",ConfiguracaoProvaRequest.class);
  assertFalse(old.assuntos().getFirst().exigirTodosPesos());
  var enabled=new ConfiguracaoProvaRequest(old.valorTotal(),old.quantidadeQuestoes(),List.of(new ConfiguracaoProvaRequest.Assunto(1L,old.assuntos().getFirst().pesos(),true)));
  assertTrue(mapper.readValue(mapper.writeValueAsString(enabled),ConfiguracaoProvaRequest.class).assuntos().getFirst().exigirTodosPesos());
 }
 @Test void optionalWeightRequirementDefaultsOffAndCanMakeAPlanIncompatible() {
  var weights=List.of(BigDecimal.ONE,new BigDecimal("2"));
  var oldConfig=new ConfiguracaoProvaRequest(new BigDecimal("3"),3,List.of(new ConfiguracaoProvaRequest.Assunto(1L,weights)));
  var enabled=new ConfiguracaoProvaRequest(new BigDecimal("3"),3,List.of(new ConfiguracaoProvaRequest.Assunto(1L,weights,true)));
  var pool=List.of(q(1,BigDecimal.ONE),q(1,BigDecimal.ONE),q(1,BigDecimal.ONE));
  assertFalse(oldConfig.assuntos().getFirst().exigirTodosPesos());
  assertTrue(ProofPlanningService.analyze(oldConfig,pool,Map.of()).possivel());
  assertNull(ProofPlanningService.analyze(enabled,pool,Map.of()).faltantes());
  var withoutWeightTwo=List.of(new ComposicaoProvaRequest(1L,BigDecimal.ONE,3));
  assertDoesNotThrow(()->ProofPlanningService.validatePlan(oldConfig,withoutWeightTwo,pool,Map.of()));
  assertThrows(DefaultErrorException.class,()->ProofPlanningService.validatePlan(enabled,withoutWeightTwo,pool,Map.of()));
 }
 @Test void enabledWeightsAreIncludedAndMissingStockIsReportedEvenForGeneral() {
  var config=new ConfiguracaoProvaRequest(new BigDecimal("4"),3,List.of(new ConfiguracaoProvaRequest.Assunto(9L,List.of(BigDecimal.ONE,new BigDecimal("2")),true)));
  var pool=List.of(q(1,BigDecimal.ONE),q(2,BigDecimal.ONE),q(3,new BigDecimal("2")));
  var result=ProofPlanningService.analyze(config,pool,Map.of(9L,"Geral"));
  assertTrue(result.possivel());assertEquals(2,result.composicao().size());
  assertTrue(result.composicao().stream().allMatch(i->i.quantidade()>=1));
  var plan=result.composicao().stream().map(i->new ComposicaoProvaRequest(i.assuntoId(),i.peso(),i.quantidade())).toList();
  assertDoesNotThrow(()->ProofPlanningService.validatePlan(config,plan,pool,Map.of(9L,"Geral")));
  var missing=ProofPlanningService.analyze(config,pool.subList(0,2),Map.of(9L,"Geral"));
  assertFalse(missing.possivel());assertEquals(1,missing.faltantes());
 }
 @Test void requiredWeightsCannotExceedQuestionCount() {
  var config=new ConfiguracaoProvaRequest(BigDecimal.TEN,3,List.of(new ConfiguracaoProvaRequest.Assunto(1L,List.of(BigDecimal.ONE,new BigDecimal("2"),new BigDecimal("3"),new BigDecimal("4")),true)));
  assertNull(ProofPlanningService.analyze(config,List.of(),Map.of()).faltantes());
 }
 @Test void generalUsesAllTopicsWhileKeepingAllowedWeights() {
  var config=new ConfiguracaoProvaRequest(new BigDecimal("3"),3,List.of(a(9,"1")));
  var pool=List.of(q(1,BigDecimal.ONE),q(2,BigDecimal.ONE),q(3,BigDecimal.ONE),q(4,new BigDecimal("2")));
  var names=Map.of(9L,"Geral");
  var result=ProofPlanningService.analyze(config,pool,names);
  assertTrue(result.possivel()); assertEquals(3,result.composicao().getFirst().disponiveis());
  var plan=List.of(new ComposicaoProvaRequest(9L,BigDecimal.ONE,3));
  assertDoesNotThrow(()->ProofPlanningService.validatePlan(config,plan,pool,names));
  assertThrows(DefaultErrorException.class,()->ProofPlanningService.validatePlan(config,plan,pool.subList(0,2),names));
 }
 @Test void generalCannotOverlapSpecificTopics() {
  var config=new ConfiguracaoProvaRequest(new BigDecimal("3"),3,List.of(a(9,"1"),a(1,"1")));
  assertThrows(DefaultErrorException.class,()->ProofPlanningService.analyze(config,List.of(),Map.of(9L,"Geral",1L,"Frações")));
 }
 private long id=0;
 private QuestaoSorteio q(long topic,BigDecimal weight){long n=++id;return new QuestaoSorteio(n,n,"discursiva",weight,"Pergunta",Map.of(),null,topic);}
 private ConfiguracaoProvaRequest.Assunto a(long id,String... weights){return new ConfiguracaoProvaRequest.Assunto(id,Arrays.stream(weights).map(BigDecimal::new).toList());}
 @Test void calculatesExactValueAndCountWithoutFloatingPointRounding(){var config=new ConfiguracaoProvaRequest(new BigDecimal("5"),4,List.of(a(1,"1.25"),a(2,"1.25")));var pool=List.of(q(1,new BigDecimal("1.25")),q(1,new BigDecimal("1.25")),q(2,new BigDecimal("1.25")),q(2,new BigDecimal("1.25")));var r=ProofPlanningService.analyze(config,pool,Map.of(1L,"Frações",2L,"Equações"));assertTrue(r.possivel());assertEquals(0,r.faltantes());assertEquals(4,r.composicao().stream().mapToInt(i->i.quantidade()).sum());assertEquals(2,r.composicao().size());}
 @Test void reportsTheMinimumMissingQuestionsForEachWeightAndSubject(){var config=new ConfiguracaoProvaRequest(new BigDecimal("5"),3,List.of(a(1,"1"),a(2,"2")));var r=ProofPlanningService.analyze(config,List.of(q(1,BigDecimal.ONE)),Map.of(1L,"Frações",2L,"Equações"));assertFalse(r.possivel());assertEquals(2,r.faltantes());assertEquals(2,r.composicao().stream().filter(i->i.assuntoId()==2L).findFirst().orElseThrow().faltantes());}
 @Test void separatesMathematicalIncompatibilityFromInsufficientStock(){var config=new ConfiguracaoProvaRequest(new BigDecimal("7"),3,List.of(a(1,"2"),a(2,"4")));var r=ProofPlanningService.analyze(config,List.of(),Map.of());assertFalse(r.possivel());assertNull(r.faltantes());assertTrue(r.composicao().isEmpty());}
 @Test void everySelectedSubjectMustParticipate(){var config=new ConfiguracaoProvaRequest(new BigDecimal("3"),3,List.of(a(1,"1"),a(2,"3")));assertNull(ProofPlanningService.analyze(config,List.of(),Map.of()).faltantes());}
 @Test void emptyBankReportsTheExactDeficitEvenWithManyAllowedWeights(){var topics=java.util.stream.LongStream.rangeClosed(1,10).mapToObj(id->a(id,"0.5","1","1.5","2")).toList();var result=ProofPlanningService.analyze(new ConfiguracaoProvaRequest(new BigDecimal("50"),50,topics),List.of(),Map.of());assertEquals(50,result.faltantes());assertEquals(10,result.composicao().stream().map(i->i.assuntoId()).distinct().count());}
 @Test void rejectsFewerThanThreeQuestions(){assertThrows(DefaultErrorException.class,()->ProofPlanningService.analyze(new ConfiguracaoProvaRequest(BigDecimal.TEN,2,List.of(a(1,"5"))),List.of(),Map.of()));}
 @Test void matchesExhaustiveSearchForSmallConfigurations(){var random=new Random(42);int[] weights={1,3,2,4};for(int sample=0;sample<100;sample++){int n=3+random.nextInt(5),target=3+random.nextInt(26);int[] stock={random.nextInt(4),random.nextInt(4),random.nextInt(4),random.nextInt(4)};var pool=new ArrayList<QuestaoSorteio>();for(int g=0;g<4;g++)for(int j=0;j<stock[g];j++)pool.add(q(g<2?1:2,BigDecimal.valueOf(weights[g]).divide(new BigDecimal("4"))));
  int best=Integer.MAX_VALUE;for(int aa=0;aa<=n;aa++)for(int bb=0;bb<=n-aa;bb++)for(int cc=0;cc<=n-aa-bb;cc++){int dd=n-aa-bb-cc;if(aa+bb==0||cc+dd==0||aa+3*bb+2*cc+4*dd!=target)continue;best=Math.min(best,Math.max(0,aa-stock[0])+Math.max(0,bb-stock[1])+Math.max(0,cc-stock[2])+Math.max(0,dd-stock[3]));}
  var result=ProofPlanningService.analyze(new ConfiguracaoProvaRequest(BigDecimal.valueOf(target).divide(new BigDecimal("4")),n,List.of(a(1,"0.25","0.75"),a(2,"0.5","1"))),pool,Map.of());if(best==Integer.MAX_VALUE)assertNull(result.faltantes());else assertEquals(best,result.faltantes(),"sample="+sample);
 }}
}

package br.com.provas.service;
import br.com.provas.model.entity.QuestaoSorteio;
import br.com.provas.exceptions.DefaultErrorException;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class QuestionDrawServiceTest {
 private long next=1;
 private QuestaoSorteio question(long topic,String weight){long id=next++;return new QuestaoSorteio(id,id,"discursiva",new BigDecimal(weight),"Pergunta",Map.of(),null,topic);}
 private List<QuestaoSorteio> full(long topic){var pool=new ArrayList<QuestaoSorteio>();for(String w:List.of("0.5","0.5","0.5","0.5","1.0","1.0","1.0","1.5","1.5","2.0"))pool.add(question(topic,w));return pool;}
 private void assertComposition(List<QuestaoSorteio> draw,Set<Long> topics){assertEquals(10,draw.size());assertEquals(10,draw.stream().map(QuestaoSorteio::id).distinct().count());assertEquals(topics,new HashSet<>(draw.stream().map(QuestaoSorteio::assuntoId).toList()));
  var expected=Map.of("0.5",4L,"1.0",3L,"1.5",2L,"2.0",1L);for(var e:expected.entrySet())assertEquals(e.getValue(),draw.stream().filter(q->q.peso().compareTo(new BigDecimal(e.getKey()))==0).count());assertEquals(0,draw.getFirst().peso().compareTo(new BigDecimal("0.5")));assertTrue(draw.indexOf(draw.stream().filter(q->q.peso().compareTo(new BigDecimal("2.0"))==0).findFirst().orElseThrow())>=6);}
 @Test void alwaysIncludesRareTopicWithoutChangingWeightsOrPositions(){var pool=full(1);pool.add(question(2,"2.0"));for(int i=0;i<200;i++)assertComposition(QuestionDrawService.draw(pool,List.of(1L,2L)),Set.of(1L,2L));}
 @Test void matchesTenSubjectsToExactlyTenCompatibleWeightSlots(){var pool=full(1);String[] slots={"0.5","0.5","0.5","1.0","1.0","1.0","1.5","1.5","2.0"};for(int i=0;i<slots.length;i++)pool.add(question(i+2,slots[i]));var topics=java.util.stream.LongStream.rangeClosed(1,10).boxed().toList();for(int i=0;i<100;i++)assertComposition(QuestionDrawService.draw(pool,topics),new HashSet<>(topics));}
 @Test void rejectsTwoTopicsThatBothRequireTheOnlyTwoPointSlot(){var pool=full(1);pool.add(question(2,"2.0"));pool.add(question(3,"2.0"));assertThrows(DefaultErrorException.class,()->QuestionDrawService.validate(pool,List.of(1L,2L,3L)));}
 @Test void doesNotTreatTheSumOfIncompleteSubjectsAsOneCompleteSubject(){var pool=full(1);var split=pool.stream().map(q->new QuestaoSorteio(q.id(),q.versaoId(),q.tipo(),q.peso(),q.enunciado(),q.alternativas(),q.respostaCorreta(),q.peso().compareTo(BigDecimal.ONE)<0?1L:2L)).toList();assertThrows(DefaultErrorException.class,()->QuestionDrawService.validate(split,List.of(1L,2L)));}
 @Test void rejectsEmptyRepeatedUnknownOrMoreThanTenSubjects(){var pool=full(1);for(List<Long> ids:List.of(List.<Long>of(),List.of(1L,1L),List.of(2L),java.util.stream.LongStream.rangeClosed(1,11).boxed().toList()))assertThrows(DefaultErrorException.class,()->QuestionDrawService.validate(pool,ids));}
 @Test void ignoresUnselectedSubjectsAndDoesNotModifySource(){var pool=full(1);pool.addAll(full(2));var original=List.copyOf(pool);assertComposition(QuestionDrawService.draw(pool,List.of(1L)),Set.of(1L));assertEquals(original,pool);}
}

package br.com.provas.validation;
import br.com.provas.model.request.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class AssuntoValidationTest {
 @Test void rejectsQuestionWithoutSubjectAndReleaseWithoutExplicitSelection(){try(var factory=Validation.buildDefaultValidatorFactory()){
  var validator=factory.getValidator();var q=new QuestaoRequest(6L,"F",8,"ano",List.of(),"discursiva",BigDecimal.ONE,"<p>Pergunta</p>",Map.of(),null,null);
  assertTrue(validator.validate(q).stream().anyMatch(e->e.getPropertyPath().toString().equals("assuntoId")));
  for(List<Long> topics:List.of(List.<Long>of(),java.util.stream.LongStream.rangeClosed(1,11).boxed().toList(),List.of(-1L))){var r=new LiberarProvaRequest("Prova",6L,Instant.now(),Instant.now().plusSeconds(3600),60,"Token",topics);assertTrue(validator.validate(r).stream().anyMatch(e->e.getPropertyPath().toString().startsWith("assuntos")));}
 }}
}

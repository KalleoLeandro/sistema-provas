package br.com.provas.service;
import br.com.provas.repository.QuestaoRepository;import br.com.provas.model.entity.PendenciaComposicao;import org.junit.jupiter.api.Test;import java.util.List;import java.math.BigDecimal;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class PendenciaQuestoesServiceTest {
 @Test void usesSavedQuantitiesInsteadOfFixedWeights(){var repo=mock(QuestaoRepository.class);when(repo.countMissingPlans(7L)).thenReturn(List.of(new PendenciaComposicao(1L,"Prova",6L,"Matemática",5L,"F",1,"A",2L,"Frações",new BigDecimal("1.25"),7,3)));var rows=new PendenciaQuestoesService(repo).findAll(7L);assertEquals(4,rows.getFirst().quantidades().getFirst().faltantes());assertEquals(1L,rows.getFirst().liberacaoId());}
 @Test void withoutReleasesThereIsNoGenericMinimum(){var repo=mock(QuestaoRepository.class);when(repo.countMissingPlans(7L)).thenReturn(List.of());assertTrue(new PendenciaQuestoesService(repo).findAll(7L).isEmpty());}
}

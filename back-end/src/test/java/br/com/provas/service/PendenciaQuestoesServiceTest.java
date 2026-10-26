package br.com.provas.service;

import br.com.provas.model.entity.QuantidadeQuestoesTurma;
import br.com.provas.repository.QuestaoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PendenciaQuestoesServiceTest {
    private final QuestaoRepository repository=mock(QuestaoRepository.class);
    private final PendenciaQuestoesService service=new PendenciaQuestoesService(repository);

    @Test void emptyClassNeedsFourThreeTwoAndOneQuestions() {
        when(repository.countByAssignedClass(7L)).thenReturn(List.of(new QuantidadeQuestoesTurma(6L,"Matemática",5L,"F",1,"A",0,0,0,0)));
        var result=service.findAll(7L);
        assertEquals(List.of(4,3,2,1),result.getFirst().quantidades().stream().map(q->q.faltantes()).toList());
        assertEquals(6L,result.getFirst().materiaId()); assertEquals(5L,result.getFirst().turmaId());
        verify(repository).countByAssignedClass(7L);
    }
    @Test void removesCompleteClassesAndDoesNotCompensateBetweenWeights() {
        when(repository.countByAssignedClass(7L)).thenReturn(List.of(
            new QuantidadeQuestoesTurma(6L,"Matemática",5L,"F",1,"A",4,3,2,1),
            new QuantidadeQuestoesTurma(6L,"Matemática",13L,"F",1,"B",10,2,0,0)));
        var result=service.findAll(7L);
        assertEquals(1,result.size()); assertEquals("B",result.getFirst().letra());
        assertEquals(List.of(0,1,2,1),result.getFirst().quantidades().stream().map(q->q.faltantes()).toList());
        assertEquals(10,result.getFirst().quantidades().getFirst().cadastradas());
    }
    @Test void noAssignedClassesMeansNoPendingRows() {
        when(repository.countByAssignedClass(7L)).thenReturn(List.of());
        assertTrue(service.findAll(7L).isEmpty());
    }
}

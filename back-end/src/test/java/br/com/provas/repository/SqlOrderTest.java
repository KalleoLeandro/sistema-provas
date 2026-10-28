package br.com.provas.repository;
import br.com.provas.model.request.OrdenacaoRequest;
import br.com.provas.exceptions.DefaultErrorException;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class SqlOrderTest {
 @Test void expandsCompositeOrderAndUsesStableId(){assertEquals(" ORDER BY t.ensino DESC,t.ano DESC,t.letra DESC,a.id DESC",SqlOrder.clause(new OrdenacaoRequest("turma","DESC"),Map.of("turma","t.ensino;t.ano;t.letra"),"a.id"));}
 @Test void rejectsUnlistedColumnsInsteadOfInterpolatingThem(){assertThrows(DefaultErrorException.class,()->SqlOrder.clause(new OrdenacaoRequest("nome;DROP TABLE alunos_2026","DESC"),Map.of("nome","a.nome"),"a.id"));}
}

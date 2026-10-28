package br.com.provas.repository;
import br.com.provas.model.request.AlunoFiltroRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.core.RowMapper;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AlunoOrderingTest {
 @Test void ordersAllFilteredRowsBeforeApplyingOffsetAndKeepsNamesWhitelisted(){
  var jdbc=mock(NamedParameterJdbcTemplate.class);var repo=new AlunoRepository(jdbc);
  when(jdbc.query(anyString(),any(MapSqlParameterSource.class),any(RowMapper.class))).thenReturn(java.util.List.of());
  repo.findFiltered(7L,new AlunoFiltroRequest("Ana","R",5L,"numero","DESC"),15,15L);
  var sql=org.mockito.ArgumentCaptor.forClass(String.class);var params=org.mockito.ArgumentCaptor.forClass(MapSqlParameterSource.class);
  verify(jdbc).query(sql.capture(),params.capture(),any(RowMapper.class));
  assertTrue(sql.getValue().contains("ORDER BY a.numero IS NULL ASC,a.numero DESC,a.id DESC LIMIT :tamanho OFFSET :offset"));
  assertTrue(sql.getValue().contains("pmt.professor_id=:professor_id"));assertEquals(7L,params.getValue().getValue("professor_id"));assertEquals("R",params.getValue().getValue("status"));assertEquals(15L,params.getValue().getValue("offset"));
 }
}

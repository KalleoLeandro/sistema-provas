package br.com.provas.repository;
import br.com.provas.model.response.AssuntoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
@Repository @RequiredArgsConstructor
public class AssuntoRepository {
 private final NamedParameterJdbcTemplate jdbc;
 private final RowMapper<AssuntoResponse> mapper=(rs,n)->new AssuntoResponse(rs.getLong("id"),rs.getLong("materia_id"),rs.getString("nome"));
 public boolean allowed(Long professor,Long materia){return !jdbc.query("SELECT pmt.materia_id FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id AND p.ativo=TRUE WHERE pmt.professor_id=:professor AND pmt.materia_id=:materia AND pmt.ativo=TRUE",new MapSqlParameterSource().addValue("professor",professor).addValue("materia",materia),(rs,n)->rs.getLong("materia_id")).isEmpty();}
 public List<AssuntoResponse> search(Long materia,String busca){return jdbc.query("SELECT id,materia_id,nome FROM assuntos WHERE materia_id=:materia AND LOCATE(:busca,nome)>0 ORDER BY nome,id",new MapSqlParameterSource().addValue("materia",materia).addValue("busca",busca),mapper);}
 public AssuntoResponse save(Long materia,String nome){
  var p=new MapSqlParameterSource().addValue("materia",materia).addValue("nome",nome);
  jdbc.update("INSERT INTO assuntos(materia_id,nome) VALUES(:materia,:nome) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)",p);
  return jdbc.query("SELECT id,materia_id,nome FROM assuntos WHERE materia_id=:materia AND nome=:nome",p,mapper).getFirst();
 }
}

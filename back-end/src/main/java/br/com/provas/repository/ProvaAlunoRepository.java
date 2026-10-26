package br.com.provas.repository;
import br.com.provas.model.response.ProvaAlunoResponse;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
@Repository @RequiredArgsConstructor
public class ProvaAlunoRepository {
 private final NamedParameterJdbcTemplate jdbc;
 private static final String FILTER="""
  FROM provas_liberacoes l JOIN alunos_2026 a ON a.id=:aluno_id AND a.status='M'
  JOIN materias m ON m.id=l.materia_id JOIN professores p ON p.id=l.professor_id
  LEFT JOIN provas_tentativas tentativa ON tentativa.aluno_id=a.id AND tentativa.liberacao_id=l.id
  WHERE (tentativa.id IS NOT NULL OR (a.turma_id=l.turma_id AND p.ativo=TRUE AND l.disponivel_em<=:agora AND l.encerra_em>:agora
   AND EXISTS(SELECT 1 FROM professores_materias_turmas pmt WHERE pmt.professor_id=l.professor_id AND pmt.materia_id=l.materia_id AND pmt.turma_id=l.turma_id AND pmt.ativo=TRUE)))
  """;
 private final RowMapper<ProvaAlunoResponse> mapper=(rs,n)->new ProvaAlunoResponse(rs.getLong("id"),rs.getString("titulo"),rs.getString("materia"),rs.getString("professor"),
  rs.getObject("disponivel_em",LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getObject("encerra_em",LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getString("situacao"),rs.getInt("duracao_minutos"),rs.getBoolean("token_configurado"));
 public long countAvailable(Long alunoId,Instant agora){return jdbc.query("SELECT COUNT(*) AS total "+FILTER,params(alunoId,agora),(rs,n)->rs.getLong("total")).getFirst();}
 public long countCompleted(Long alunoId,Instant agora){return jdbc.query("SELECT COUNT(*) AS total "+FILTER+" AND tentativa.submetida_em IS NOT NULL",params(alunoId,agora),(rs,n)->rs.getLong("total")).getFirst();}
 public List<ProvaAlunoResponse> findAvailable(Long alunoId,Instant agora,int tamanho,long offset){return jdbc.query("""
  SELECT l.id,COALESCE(tentativa.titulo,l.titulo) AS titulo,COALESCE(tentativa.materia,m.nome) AS materia,p.nome AS professor,
   COALESCE(tentativa.iniciada_em,l.disponivel_em) AS disponivel_em,COALESCE(tentativa.encerra_em,l.encerra_em) AS encerra_em,
   CASE WHEN tentativa.submetida_em IS NOT NULL THEN 'submetida' WHEN tentativa.id IS NOT NULL THEN 'em_andamento' ELSE 'pendente' END AS situacao,
   l.duracao_minutos,l.token_hash IS NOT NULL AS token_configurado
  """+FILTER+" ORDER BY (tentativa.submetida_em IS NOT NULL),encerra_em,l.id LIMIT :tamanho OFFSET :offset",params(alunoId,agora).addValue("tamanho",tamanho).addValue("offset",offset),mapper);}
 private MapSqlParameterSource params(Long alunoId,Instant agora){return new MapSqlParameterSource().addValue("aluno_id",alunoId).addValue("agora",LocalDateTime.ofInstant(agora,ZoneOffset.UTC));}
}

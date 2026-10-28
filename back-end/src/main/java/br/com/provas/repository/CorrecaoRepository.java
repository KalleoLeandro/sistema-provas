package br.com.provas.repository;
import br.com.provas.model.request.CorrigirQuestaoRequest;
import br.com.provas.model.response.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
@Repository @RequiredArgsConstructor
public class CorrecaoRepository {
 private final NamedParameterJdbcTemplate jdbc;
 private static final String SELECT="""
  SELECT a.id,a.liberacao_id,a.titulo,a.materia,s.nome AS aluno,s.cgm,l.turma_id,t.ensino,t.ano,t.letra,a.submetida_em,
   (SELECT COUNT(*) FROM provas_tentativas_questoes q WHERE q.tentativa_id=a.id AND q.tipo='discursiva' AND q.resposta IS NOT NULL AND TRIM(q.resposta)<>'' AND q.pontuacao IS NULL) AS pendentes,
   (SELECT COALESCE(SUM(q.pontuacao),0) FROM provas_tentativas_questoes q WHERE q.tentativa_id=a.id) AS nota_parcial
  FROM provas_tentativas a JOIN provas_liberacoes l ON l.id=a.liberacao_id
  JOIN alunos_2026 s ON s.id=a.aluno_id JOIN turmas t ON t.id=l.turma_id
  WHERE l.professor_id=:professor AND a.submetida_em IS NOT NULL
  """;
 private static final String FILTER=" AND (:turma IS NULL OR l.turma_id=:turma)";
 private static final String HAVING=" HAVING (:situacao='todas' OR (:situacao='pendentes' AND pendentes>0) OR (:situacao='corrigidas' AND pendentes=0))";
 private final RowMapper<ProvaCorrecaoResumoResponse> mapper=(rs,n)->new ProvaCorrecaoResumoResponse(rs.getLong("id"),rs.getLong("liberacao_id"),rs.getString("titulo"),rs.getString("materia"),rs.getString("aluno"),rs.getString("cgm"),rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"),instant(rs,"submetida_em"),rs.getInt("pendentes"),rs.getBigDecimal("nota_parcial"));
 private Instant instant(java.sql.ResultSet rs,String column)throws java.sql.SQLException{var value=rs.getObject(column,LocalDateTime.class);return value==null?null:value.toInstant(ZoneOffset.UTC);}
 private MapSqlParameterSource params(Long professor,Long turma,String situacao){return new MapSqlParameterSource().addValue("professor",professor).addValue("turma",turma).addValue("situacao",situacao);}
 public long count(Long professor,Long turma,String situacao){return jdbc.query("SELECT COUNT(*) AS total FROM ("+SELECT+FILTER+HAVING+") consulta",params(professor,turma,situacao),(rs,n)->rs.getLong("total")).getFirst();}
 public List<ProvaCorrecaoResumoResponse> findAll(Long professor,Long turma,String situacao,int size,long offset){return jdbc.query(SELECT+FILTER+HAVING+" ORDER BY (pendentes>0) DESC,a.submetida_em DESC,a.id DESC LIMIT :size OFFSET :offset",params(professor,turma,situacao).addValue("size",size).addValue("offset",offset),mapper);}
 public List<ProvaCorrecaoResumoResponse> findAll(Long professor,Long turma,String situacao,int size,long offset,br.com.provas.model.request.OrdenacaoRequest order){return jdbc.query(SELECT+FILTER+HAVING+SqlOrder.clause(order,java.util.Map.of("aluno","s.nome","titulo","a.titulo","materia","a.materia","turma","t.ensino;t.ano;t.letra","entregueEm","a.submetida_em","situacao","CASE WHEN pendentes>0 THEN 'Pendente' ELSE 'Corrigida' END","nota","nota_parcial"),"a.id")+" LIMIT :size OFFSET :offset",params(professor,turma,situacao).addValue("size",size).addValue("offset",offset),mapper);}
 public Optional<ProvaCorrecaoResumoResponse> findById(Long professor,Long id){return jdbc.query(SELECT+" AND a.id=:id",params(professor,null,"todas").addValue("id",id),mapper).stream().findFirst();}
 public List<QuestaoCorrecaoResponse> questions(Long professor,Long id){return jdbc.query("""
  SELECT q.* FROM provas_tentativas_questoes q JOIN provas_tentativas a ON a.id=q.tentativa_id
  JOIN provas_liberacoes l ON l.id=a.liberacao_id WHERE q.tentativa_id=:id AND l.professor_id=:professor AND a.submetida_em IS NOT NULL ORDER BY q.numero
  """,params(professor,null,"todas").addValue("id",id),(rs,n)->{
   var alternatives=new LinkedHashMap<String,String>();for(String letter:List.of("A","B","C","D","E")){var value=rs.getString("alternativa_"+letter.toLowerCase(Locale.ROOT));if(value!=null)alternatives.put(letter,value);}
   return new QuestaoCorrecaoResponse(rs.getInt("numero"),rs.getString("tipo"),rs.getBigDecimal("peso"),rs.getString("enunciado"),alternatives,rs.getString("resposta_correta"),rs.getString("resposta"),rs.getBigDecimal("pontuacao"),rs.getString("feedback"),rs.getLong("correcao_revisao"),instant(rs,"corrigida_em"));});}
 public boolean update(Long professor,Long id,int numero,CorrigirQuestaoRequest request,Instant now){return jdbc.update("""
  UPDATE provas_tentativas_questoes q JOIN provas_tentativas a ON a.id=q.tentativa_id JOIN provas_liberacoes l ON l.id=a.liberacao_id
  SET q.pontuacao=:nota,q.feedback=:feedback,q.correcao_revisao=q.correcao_revisao+1,q.corrigida_em=:agora,q.corrigida_por=:professor
  WHERE q.tentativa_id=:id AND q.numero=:numero AND q.correcao_revisao=:revisao AND l.professor_id=:professor AND a.submetida_em IS NOT NULL
  """,params(professor,null,"todas").addValue("id",id).addValue("numero",numero).addValue("nota",request.pontuacao()).addValue("feedback",request.feedback()).addValue("revisao",request.revisao()).addValue("agora",LocalDateTime.ofInstant(now,ZoneOffset.UTC)))==1;}
}

package br.com.provas.repository;

import br.com.provas.model.entity.*;
import br.com.provas.model.response.QuestaoExecucaoResponse;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository @RequiredArgsConstructor
public class ExecucaoProvaRepository {
 private final NamedParameterJdbcTemplate jdbc;private final tools.jackson.databind.ObjectMapper json;
 private Instant instant(java.sql.ResultSet rs,String field)throws java.sql.SQLException {var d=rs.getObject(field,LocalDateTime.class);return d==null?null:d.toInstant(ZoneOffset.UTC);}
 private LocalDateTime date(Instant value){return LocalDateTime.ofInstant(value,ZoneOffset.UTC);}
 private MapSqlParameterSource params(Long aluno,Long lib){return new MapSqlParameterSource().addValue("aluno",aluno).addValue("lib",lib);}
 private final RowMapper<TentativaProva> attemptMapper=(rs,n)->new TentativaProva(rs.getLong("id"),rs.getLong("liberacao_id"),rs.getLong("aluno_id"),rs.getString("titulo"),rs.getString("materia"),instant(rs,"iniciada_em"),instant(rs,"encerra_em"),instant(rs,"submetida_em"));
 private Map<String,String> alternatives(java.sql.ResultSet rs)throws java.sql.SQLException {var result=new LinkedHashMap<String,String>();for(String l:List.of("A","B","C","D","E")){var text=rs.getString("alternativa_"+l.toLowerCase(Locale.ROOT));if(text!=null)result.put(l,text);}return result;}
 public Optional<TentativaProva> findAttempt(Long aluno,Long lib){return jdbc.query("SELECT * FROM provas_tentativas WHERE aluno_id=:aluno AND liberacao_id=:lib",params(aluno,lib),attemptMapper).stream().findFirst();}
 public Optional<ConfiguracaoInicioProva> configuration(Long aluno,Long lib){return configuration(aluno,lib,false);}
 public Optional<ConfiguracaoInicioProva> configuration(Long aluno,Long lib,boolean lock){
  return jdbc.query("""
   SELECT l.*,m.nome AS materia,t.ensino,t.ano FROM provas_liberacoes l
   JOIN alunos_2026 a ON a.turma_id=l.turma_id AND a.id=:aluno AND a.status='M'
   JOIN turmas t ON t.id=l.turma_id JOIN materias m ON m.id=l.materia_id
   JOIN professores p ON p.id=l.professor_id AND p.ativo=TRUE
   WHERE l.id=:lib AND EXISTS(SELECT 1 FROM professores_materias_turmas pmt WHERE pmt.professor_id=l.professor_id AND pmt.materia_id=l.materia_id AND pmt.turma_id=l.turma_id AND pmt.ativo=TRUE)
   """+(lock?" FOR SHARE":""),params(aluno,lib),(rs,n)->new ConfiguracaoInicioProva(rs.getLong("id"),rs.getLong("professor_id"),rs.getLong("materia_id"),rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("titulo"),rs.getString("materia"),instant(rs,"disponivel_em"),instant(rs,"encerra_em"),rs.getInt("duracao_minutos"),rs.getString("token_hash"),rs.getString("composicao")==null?List.of():Arrays.asList(json.readValue(rs.getString("composicao"),br.com.provas.model.request.ComposicaoProvaRequest[].class)),rs.getBigDecimal("valor_total"),rs.getInt("quantidade_questoes"))).stream().findFirst();
 }
 public java.math.BigDecimal attemptValue(Long id){return jdbc.query("SELECT COALESCE(SUM(peso),0) AS valor FROM provas_tentativas_questoes WHERE tentativa_id=:id",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getBigDecimal("valor")).getFirst();}
 public int attemptCount(Long id){return jdbc.query("SELECT COUNT(*) AS total FROM provas_tentativas_questoes WHERE tentativa_id=:id",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getInt("total")).getFirst();}
 public List<Long> subjects(Long lib){return jdbc.query("SELECT assunto_id FROM provas_liberacoes_assuntos WHERE liberacao_id=:lib ORDER BY assunto_id",new MapSqlParameterSource().addValue("lib",lib),(rs,n)->rs.getLong("assunto_id"));}
 public List<QuestaoSorteio> candidates(ConfiguracaoInicioProva c){
  return jdbc.query("""
   SELECT q.*,v.id AS versao_id,
     COALESCE((SELECT a.id FROM assuntos a JOIN provas_liberacoes_assuntos pla ON pla.assunto_id=a.id
       WHERE pla.liberacao_id=:lib AND a.materia_id=:materia AND a.nome='Geral' LIMIT 1),q.assunto_id) AS assunto_sorteio
   FROM questoes q JOIN questoes_versoes v ON v.questao_id=q.id
     AND v.numero=(SELECT MAX(v2.numero) FROM questoes_versoes v2 WHERE v2.questao_id=q.id)
   WHERE q.professor_id=:professor AND q.materia_id=:materia AND q.ensino=:ensino AND q.ano=:ano
     AND (q.abrangencia='ano' OR EXISTS(SELECT 1 FROM questoes_turmas qt WHERE qt.questao_id=q.id AND qt.turma_id=:turma))
   ORDER BY q.id FOR SHARE
   """,new MapSqlParameterSource().addValue("lib",c.id()).addValue("professor",c.professorId()).addValue("materia",c.materiaId()).addValue("ensino",c.ensino()).addValue("ano",c.ano()).addValue("turma",c.turmaId()),
   (rs,n)->new QuestaoSorteio(rs.getLong("id"),rs.getLong("versao_id"),rs.getString("tipo"),rs.getBigDecimal("peso"),rs.getString("enunciado"),alternatives(rs),rs.getString("resposta_correta"),rs.getLong("assunto_sorteio")));
 }
 public TentativaProva create(Long aluno,ConfiguracaoInicioProva c,Instant inicio,Instant fim,List<QuestaoSorteio> questions){
  var p=params(aluno,c.id()).addValue("titulo",c.titulo()).addValue("materia",c.materia()).addValue("inicio",date(inicio)).addValue("fim",date(fim));var key=new GeneratedKeyHolder();
  jdbc.update("INSERT INTO provas_tentativas(aluno_id,liberacao_id,titulo,materia,iniciada_em,encerra_em) VALUES(:aluno,:lib,:titulo,:materia,:inicio,:fim)",p,key,new String[]{"id"});
  long id=Objects.requireNonNull(key.getKey()).longValue();var values=new ArrayList<MapSqlParameterSource>();int number=0;
  for(var q:questions){var row=new MapSqlParameterSource().addValue("tentativa",id).addValue("numero",++number).addValue("questao",q.id()).addValue("versao",q.versaoId()).addValue("tipo",q.tipo()).addValue("peso",q.peso()).addValue("enunciado",q.enunciado()).addValue("gabarito",q.respostaCorreta());
   for(String l:List.of("A","B","C","D","E"))row.addValue(l,q.alternativas().get(l));values.add(row);}
  jdbc.batchUpdate("""
   INSERT INTO provas_tentativas_questoes(tentativa_id,numero,questao_id,versao_id,tipo,peso,enunciado,alternativa_a,alternativa_b,alternativa_c,alternativa_d,alternativa_e,resposta_correta)
   VALUES(:tentativa,:numero,:questao,:versao,:tipo,:peso,:enunciado,:A,:B,:C,:D,:E,:gabarito)
   """,values.toArray(MapSqlParameterSource[]::new));return findAttempt(aluno,c.id()).orElseThrow();
 }
 public List<QuestaoExecucaoResponse> questions(Long id){return jdbc.query("SELECT * FROM provas_tentativas_questoes WHERE tentativa_id=:id ORDER BY numero",new MapSqlParameterSource().addValue("id",id),
  (rs,n)->new QuestaoExecucaoResponse(rs.getInt("numero"),rs.getString("tipo"),rs.getBigDecimal("peso"),rs.getString("enunciado"),alternatives(rs),rs.getString("resposta"),rs.getLong("revisao")));}
 public boolean answer(Long id,int number,String resposta,long revisao){return jdbc.update("UPDATE provas_tentativas_questoes SET resposta=:resposta,revisao=revisao+1 WHERE tentativa_id=:id AND numero=:numero AND revisao=:revisao",
  new MapSqlParameterSource().addValue("id",id).addValue("numero",number).addValue("resposta",resposta).addValue("revisao",revisao))==1;}
 public void markVersions(Long id){jdbc.update("UPDATE questoes_versoes v JOIN provas_tentativas_questoes q ON q.versao_id=v.id SET v.utilizada_em_prova_respondida=TRUE WHERE q.tentativa_id=:id",new MapSqlParameterSource().addValue("id",id));}
 public void submit(Long id,Instant when,boolean automatic){
  var p=new MapSqlParameterSource().addValue("id",id).addValue("quando",date(when)).addValue("auto",automatic);
  jdbc.update("UPDATE provas_tentativas SET submetida_em=:quando,automatica=:auto WHERE id=:id AND submetida_em IS NULL",p);
  jdbc.update("UPDATE provas_tentativas_questoes SET pontuacao=CASE WHEN resposta IS NULL OR TRIM(resposta)='' THEN 0 WHEN tipo='objetiva' THEN CASE WHEN resposta=resposta_correta THEN peso ELSE 0 END ELSE NULL END WHERE tentativa_id=:id",p);
  markVersions(id);
 }
 public List<Long> expiredStudents(Instant now){return jdbc.query("SELECT DISTINCT aluno_id FROM provas_tentativas WHERE submetida_em IS NULL AND encerra_em<=:agora LIMIT 100",new MapSqlParameterSource().addValue("agora",date(now)),(rs,n)->rs.getLong("aluno_id"));}
 public List<Long> expiredReleases(Long aluno,Instant now){return jdbc.query("SELECT liberacao_id FROM provas_tentativas WHERE aluno_id=:aluno AND submetida_em IS NULL AND encerra_em<=:agora ORDER BY id",params(aluno,null).addValue("agora",date(now)),(rs,n)->rs.getLong("liberacao_id"));}
 public Optional<TentativaProva> lockForSubmission(Long aluno,Long lib){return jdbc.query("SELECT * FROM provas_tentativas WHERE aluno_id=:aluno AND liberacao_id=:lib FOR UPDATE",params(aluno,lib),attemptMapper).stream().findFirst();}
 public void persistAnswers(EstadoProvaRedis state){
  var parameters=new ArrayList<MapSqlParameterSource>();for(var q:state.copia().questoes()){var answer=state.respostas().get(q.numero());parameters.add(new MapSqlParameterSource().addValue("id",state.tentativaId()).addValue("numero",q.numero()).addValue("resposta",answer==null||answer.isBlank()?null:answer).addValue("revisao",state.revisoes().get(q.numero())));}
  jdbc.batchUpdate("UPDATE provas_tentativas_questoes SET resposta=:resposta,revisao=:revisao WHERE tentativa_id=:id AND numero=:numero",parameters.toArray(MapSqlParameterSource[]::new));
 }
}

package br.com.provas.repository;

import br.com.provas.model.entity.LiberacaoProva;
import br.com.provas.model.request.LiberarProvaRequest;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProvaRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final tools.jackson.databind.ObjectMapper json;
    private final RowMapper<LiberacaoProva> mapper=(rs,n)->new LiberacaoProva(rs.getLong("id"),rs.getString("titulo"),rs.getLong("materia_id"),rs.getString("materia"),
        rs.getObject("disponivel_em",java.time.LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getObject("encerra_em",java.time.LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getInt("duracao_minutos"),rs.getString("token_hash")!=null,rs.getString("token_cifrado"),readConfig(rs.getString("configuracao")),readPlan(rs.getString("composicao")),rs.getBigDecimal("valor_total"),rs.getInt("quantidade_questoes"));

    private br.com.provas.model.request.ConfiguracaoProvaRequest readConfig(String value){return value==null?null:json.readValue(value,br.com.provas.model.request.ConfiguracaoProvaRequest.class);}
    private java.util.List<br.com.provas.model.request.ComposicaoProvaRequest> readPlan(String value){return value==null?java.util.List.of():java.util.Arrays.asList(json.readValue(value,br.com.provas.model.request.ComposicaoProvaRequest[].class));}
    private MapSqlParameterSource configuration(MapSqlParameterSource params,LiberarProvaRequest request){return params.addValue("valor",request.configuracao().valorTotal()).addValue("quantidade",request.configuracao().quantidadeQuestoes()).addValue("configuracao",json.writeValueAsString(request.configuracao())).addValue("composicao",json.writeValueAsString(request.composicao()));}
    public boolean hasSubjectAssignment(Long professor,Long turma,Long materia){return !jdbc.query("SELECT professor_id FROM professores_materias_turmas WHERE professor_id=:professor AND turma_id=:turma AND materia_id=:materia AND ativo=TRUE",new MapSqlParameterSource().addValue("professor",professor).addValue("turma",turma).addValue("materia",materia),(rs,n)->rs.getLong("professor_id")).isEmpty();}
    public boolean hasClass(Long professorId,Long turmaId) {
        return !jdbc.query("SELECT pmt.turma_id FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id WHERE pmt.professor_id=:professor_id AND pmt.turma_id=:turma_id AND pmt.ativo=TRUE AND p.ativo=TRUE",
            params(professorId,turmaId),(rs,n)->rs.getLong("turma_id")).isEmpty();
    }
    public Long findStudentClass(Long professorId,Long alunoId) {
        return jdbc.query("SELECT a.turma_id FROM alunos_2026 a WHERE a.id=:aluno_id AND EXISTS (SELECT 1 FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id WHERE pmt.professor_id=:professor_id AND pmt.turma_id=a.turma_id AND pmt.ativo=TRUE AND p.ativo=TRUE)",
            new MapSqlParameterSource().addValue("professor_id",professorId).addValue("aluno_id",alunoId),(rs,n)->rs.getLong("turma_id")).stream().findFirst().orElse(null);
    }
    public boolean lockClassSubject(Long professorId,Long turmaId,Long materiaId) {
        return !jdbc.query("SELECT pmt.turma_id FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id WHERE pmt.professor_id=:professor_id AND pmt.turma_id=:turma_id AND pmt.materia_id=:materia_id AND pmt.ativo=TRUE AND p.ativo=TRUE FOR SHARE",
            params(professorId,turmaId).addValue("materia_id",materiaId),(rs,n)->rs.getLong("turma_id")).isEmpty();
    }
    public long count(Long professorId,Long turmaId) {
        return jdbc.query("SELECT COUNT(*) AS total FROM provas_liberacoes WHERE professor_id=:professor_id AND turma_id=:turma_id",params(professorId,turmaId),(rs,n)->rs.getLong("total")).getFirst();
    }
    public List<LiberacaoProva> findAll(Long professorId,Long turmaId,int tamanho,long offset) {
        return jdbc.query("""
            SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id
            WHERE l.professor_id=:professor_id AND l.turma_id=:turma_id ORDER BY l.id DESC LIMIT :tamanho OFFSET :offset
            """,params(professorId,turmaId).addValue("tamanho",tamanho).addValue("offset",offset),mapper);
    }
    public List<LiberacaoProva> findAll(Long professorId,Long turmaId,int tamanho,long offset,br.com.provas.model.request.OrdenacaoRequest order,java.time.Instant now){
        return jdbc.query("SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id WHERE l.professor_id=:professor_id AND l.turma_id=:turma_id"+
            SqlOrder.clause(order,java.util.Map.of("titulo","l.titulo","materia","m.nome","disponivelEm","l.disponivel_em","encerraEm","l.encerra_em","situacao","CASE WHEN l.disponivel_em>:agora THEN 'Agendada' WHEN l.encerra_em<=:agora THEN 'Encerrada' ELSE 'Disponível' END"),"l.id")+" LIMIT :tamanho OFFSET :offset",
            params(professorId,turmaId).addValue("agora",java.time.LocalDateTime.ofInstant(now,ZoneOffset.UTC)).addValue("tamanho",tamanho).addValue("offset",offset),mapper);
    }
    public LiberacaoProva save(Long professorId,Long turmaId,LiberarProvaRequest request,String tokenHash,String tokenCifrado) {
        var parameters=configuration(params(professorId,turmaId),request).addValue("turma_id",turmaId).addValue("materia_id",request.materiaId()).addValue("titulo",request.titulo()).addValue("duracao",request.duracaoMinutos()).addValue("token_hash",tokenHash).addValue("token_cifrado",tokenCifrado)
            .addValue("disponivel_em",java.time.LocalDateTime.ofInstant(request.disponivelEm(),ZoneOffset.UTC))
            .addValue("encerra_em",java.time.LocalDateTime.ofInstant(request.encerraEm(),ZoneOffset.UTC));
        var key=new GeneratedKeyHolder();
        jdbc.update("""
            INSERT INTO provas_liberacoes(professor_id,turma_id,materia_id,titulo,disponivel_em,encerra_em,duracao_minutos,token_hash,token_cifrado,valor_total,quantidade_questoes,configuracao,composicao)
            VALUES(:professor_id,:turma_id,:materia_id,:titulo,:disponivel_em,:encerra_em,:duracao,:token_hash,:token_cifrado,:valor,:quantidade,:configuracao,:composicao)
            """,parameters,key,new String[]{"id"});
        return jdbc.query("SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id WHERE l.id=:id",
            new MapSqlParameterSource().addValue("id",java.util.Objects.requireNonNull(key.getKey()).longValue()),mapper).getFirst();
    }
    public List<br.com.provas.model.response.AssuntoResponse> subjects(Long lib){return jdbc.query("SELECT a.id,a.materia_id,a.nome FROM assuntos a JOIN provas_liberacoes_assuntos pla ON pla.assunto_id=a.id WHERE pla.liberacao_id=:lib ORDER BY a.nome,a.id",new MapSqlParameterSource().addValue("lib",lib),(rs,n)->new br.com.provas.model.response.AssuntoResponse(rs.getLong("id"),rs.getLong("materia_id"),rs.getString("nome")));}
    public List<br.com.provas.model.response.AssuntoResponse> subjectOptions(Long professor,Long turma,Long materia){
        return jdbc.query("SELECT a.id,a.materia_id,a.nome FROM assuntos a WHERE a.materia_id=:materia AND EXISTS(SELECT 1 FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id AND p.ativo=TRUE WHERE pmt.professor_id=:professor AND pmt.turma_id=:turma AND pmt.materia_id=a.materia_id AND pmt.ativo=TRUE) ORDER BY a.nome,a.id",new MapSqlParameterSource().addValue("professor",professor).addValue("turma",turma).addValue("materia",materia),(rs,n)->new br.com.provas.model.response.AssuntoResponse(rs.getLong("id"),rs.getLong("materia_id"),rs.getString("nome")));
    }
    public void ensureGeneralSubject(Long materia) {
        jdbc.update("INSERT INTO assuntos(materia_id,nome) VALUES(:materia,'Geral') ON DUPLICATE KEY UPDATE id=id",new MapSqlParameterSource("materia",materia));
    }
    public int validSubjects(Long materia,List<Long> ids){return jdbc.query("SELECT id FROM assuntos WHERE materia_id=:materia AND id IN (:ids) FOR SHARE",new MapSqlParameterSource().addValue("materia",materia).addValue("ids",ids),(rs,n)->rs.getLong("id")).size();}
    public List<br.com.provas.model.entity.QuestaoSorteio> subjectCandidates(Long professor,Long turma,Long materia){
        return jdbc.query("SELECT q.id,q.tipo,q.peso,q.assunto_id FROM questoes q JOIN turmas t ON t.id=:turma AND t.ensino=q.ensino AND t.ano=q.ano WHERE q.professor_id=:professor AND q.materia_id=:materia AND (q.abrangencia='ano' OR EXISTS(SELECT 1 FROM questoes_turmas qt WHERE qt.questao_id=q.id AND qt.turma_id=t.id)) ORDER BY q.id",new MapSqlParameterSource().addValue("professor",professor).addValue("turma",turma).addValue("materia",materia),(rs,n)->new br.com.provas.model.entity.QuestaoSorteio(rs.getLong("id"),rs.getLong("id"),rs.getString("tipo"),rs.getBigDecimal("peso"),"",java.util.Map.of(),null,rs.getLong("assunto_id")));
    }
    public void saveSubjects(Long lib,List<Long> ids){
        var p=new MapSqlParameterSource().addValue("lib",lib);jdbc.update("DELETE FROM provas_liberacoes_assuntos WHERE liberacao_id=:lib",p);
        jdbc.batchUpdate("INSERT INTO provas_liberacoes_assuntos(liberacao_id,assunto_id) VALUES(:lib,:assunto)",ids.stream().sorted().map(id->new MapSqlParameterSource().addValue("lib",lib).addValue("assunto",id)).toArray(MapSqlParameterSource[]::new));
    }
    private MapSqlParameterSource params(Long professorId,Long turmaId) { return new MapSqlParameterSource().addValue("professor_id",professorId).addValue("turma_id",turmaId); }
    public boolean update(Long professorId,Long turmaId,Long id,LiberarProvaRequest request,String tokenHash,String tokenCifrado){
        var p=configuration(params(professorId,turmaId),request).addValue("id",id).addValue("titulo",request.titulo()).addValue("materia_id",request.materiaId())
          .addValue("inicio",java.time.LocalDateTime.ofInstant(request.disponivelEm(),ZoneOffset.UTC)).addValue("fim",java.time.LocalDateTime.ofInstant(request.encerraEm(),ZoneOffset.UTC))
          .addValue("duracao",request.duracaoMinutos()).addValue("token_hash",tokenHash).addValue("token_cifrado",tokenCifrado);
        return jdbc.update("UPDATE provas_liberacoes SET valor_total=:valor,quantidade_questoes=:quantidade,configuracao=:configuracao,composicao=:composicao,titulo=:titulo,materia_id=:materia_id,disponivel_em=:inicio,encerra_em=:fim,duracao_minutos=:duracao,token_hash=COALESCE(:token_hash,token_hash),token_cifrado=COALESCE(:token_cifrado,token_cifrado) WHERE id=:id AND professor_id=:professor_id AND turma_id=:turma_id",p)==1;
    }
    public java.util.Optional<LiberacaoProva> findById(Long professorId,Long turmaId,Long id){
        return jdbc.query("SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id WHERE l.id=:id AND l.professor_id=:professor_id AND l.turma_id=:turma_id",params(professorId,turmaId).addValue("id",id),mapper).stream().findFirst();
    }
}

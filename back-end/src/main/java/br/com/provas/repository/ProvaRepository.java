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
    private final RowMapper<LiberacaoProva> mapper=(rs,n)->new LiberacaoProva(rs.getLong("id"),rs.getString("titulo"),rs.getLong("materia_id"),rs.getString("materia"),
        rs.getObject("disponivel_em",java.time.LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getObject("encerra_em",java.time.LocalDateTime.class).toInstant(ZoneOffset.UTC),rs.getInt("duracao_minutos"),rs.getString("token_hash")!=null,rs.getString("token_cifrado"));

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
    public LiberacaoProva save(Long professorId,Long turmaId,LiberarProvaRequest request,String tokenHash,String tokenCifrado) {
        var parameters=params(professorId,turmaId).addValue("turma_id",turmaId).addValue("materia_id",request.materiaId()).addValue("titulo",request.titulo()).addValue("duracao",request.duracaoMinutos()).addValue("token_hash",tokenHash).addValue("token_cifrado",tokenCifrado)
            .addValue("disponivel_em",java.time.LocalDateTime.ofInstant(request.disponivelEm(),ZoneOffset.UTC))
            .addValue("encerra_em",java.time.LocalDateTime.ofInstant(request.encerraEm(),ZoneOffset.UTC));
        var key=new GeneratedKeyHolder();
        jdbc.update("""
            INSERT INTO provas_liberacoes(professor_id,turma_id,materia_id,titulo,disponivel_em,encerra_em,duracao_minutos,token_hash,token_cifrado)
            VALUES(:professor_id,:turma_id,:materia_id,:titulo,:disponivel_em,:encerra_em,:duracao,:token_hash,:token_cifrado)
            """,parameters,key,new String[]{"id"});
        return jdbc.query("SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id WHERE l.id=:id",
            new MapSqlParameterSource().addValue("id",java.util.Objects.requireNonNull(key.getKey()).longValue()),mapper).getFirst();
    }
    private MapSqlParameterSource params(Long professorId,Long turmaId) { return new MapSqlParameterSource().addValue("professor_id",professorId).addValue("turma_id",turmaId); }
    public boolean update(Long professorId,Long turmaId,Long id,LiberarProvaRequest request,String tokenHash,String tokenCifrado){
        var p=params(professorId,turmaId).addValue("id",id).addValue("titulo",request.titulo()).addValue("materia_id",request.materiaId())
          .addValue("inicio",java.time.LocalDateTime.ofInstant(request.disponivelEm(),ZoneOffset.UTC)).addValue("fim",java.time.LocalDateTime.ofInstant(request.encerraEm(),ZoneOffset.UTC))
          .addValue("duracao",request.duracaoMinutos()).addValue("token_hash",tokenHash).addValue("token_cifrado",tokenCifrado);
        return jdbc.update("UPDATE provas_liberacoes SET titulo=:titulo,materia_id=:materia_id,disponivel_em=:inicio,encerra_em=:fim,duracao_minutos=:duracao,token_hash=COALESCE(:token_hash,token_hash),token_cifrado=COALESCE(:token_cifrado,token_cifrado) WHERE id=:id AND professor_id=:professor_id AND turma_id=:turma_id",p)==1;
    }
    public java.util.Optional<LiberacaoProva> findById(Long professorId,Long turmaId,Long id){
        return jdbc.query("SELECT l.*,m.nome AS materia FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id WHERE l.id=:id AND l.professor_id=:professor_id AND l.turma_id=:turma_id",params(professorId,turmaId).addValue("id",id),mapper).stream().findFirst();
    }
}

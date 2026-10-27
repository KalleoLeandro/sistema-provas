package br.com.provas.repository;

import br.com.provas.model.entity.Turma;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TurmaRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Turma> turmaMapper = (rs, rowNum) -> new Turma(
        rs.getLong("id"), rs.getString("ensino"), rs.getInt("ano"), rs.getString("letra"));

    public List<Turma> findAll() {
        String sql = "SELECT id, ensino, ano, letra FROM turmas ORDER BY ensino, ano, letra, id";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), turmaMapper);
    }

    public java.util.Optional<Turma> findById(Long id,boolean lock){
        return namedParameterJdbcTemplate.query("SELECT id,ensino,ano,letra FROM turmas WHERE id=:id"+(lock?" FOR UPDATE":""),new MapSqlParameterSource().addValue("id",id),turmaMapper).stream().findFirst();
    }
    public boolean hasPeople(Long id){
        return namedParameterJdbcTemplate.query("SELECT (EXISTS(SELECT 1 FROM alunos_2026 WHERE turma_id=:id) OR EXISTS(SELECT 1 FROM professores_turmas WHERE turma_id=:id) OR EXISTS(SELECT 1 FROM professores_materias_turmas WHERE turma_id=:id)) AS vinculado",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getBoolean("vinculado")).getFirst();
    }
    public void update(Long id,br.com.provas.model.request.TurmaRequest request){
        namedParameterJdbcTemplate.update("UPDATE turmas SET ensino=:ensino,ano=:ano,letra=:letra WHERE id=:id",new MapSqlParameterSource().addValue("id",id).addValue("ensino",request.ensino()).addValue("ano",request.ano()).addValue("letra",request.letra()));
    }
    public void delete(Long id){namedParameterJdbcTemplate.update("DELETE FROM turmas WHERE id=:id",new MapSqlParameterSource().addValue("id",id));}
    public Turma save(String ensino, Integer ano, String letra) {
        String sql = "INSERT INTO turmas (ensino, ano, letra) VALUES (:ensino, :ano, :letra)";
        var params = new MapSqlParameterSource().addValue("ensino", ensino).addValue("ano", ano).addValue("letra", letra);
        var keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });
        long id = Objects.requireNonNull(keyHolder.getKey(), "ID não retornado pelo banco.").longValue();
        String sqlSelect = "SELECT id, ensino, ano, letra FROM turmas WHERE id = :id";
        return namedParameterJdbcTemplate.queryForObject(sqlSelect, new MapSqlParameterSource().addValue("id", id), turmaMapper);
    }
}

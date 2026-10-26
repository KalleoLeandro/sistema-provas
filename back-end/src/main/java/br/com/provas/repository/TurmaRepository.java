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

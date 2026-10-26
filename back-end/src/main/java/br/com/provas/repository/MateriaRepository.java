package br.com.provas.repository;

import br.com.provas.model.entity.Materia;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MateriaRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final RowMapper<Materia> materiaMapper = (rs, rowNum) ->
        new Materia(rs.getLong("id"), rs.getString("nome"));

    public List<Materia> findAll() {
        String sql = "SELECT id, nome FROM materias ORDER BY nome, id";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), materiaMapper);
    }

    public Optional<Materia> findById(Long id) {
        String sql = "SELECT id, nome FROM materias WHERE id = :id";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource().addValue("id", id), materiaMapper)
            .stream().findFirst();
    }

    public int update(Long id, String nome) {
        String sql = "UPDATE materias SET nome = :nome WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id).addValue("nome", nome));
    }

    public int delete(Long id) {
        String sql = "DELETE FROM materias WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id));
    }

    public Materia save(String nome) {
        String sql = "INSERT INTO materias (nome) VALUES (:nome)";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("nome", nome);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });
        long id = Objects.requireNonNull(keyHolder.getKey(), "ID não retornado pelo banco.").longValue();
        String sqlSelect = "SELECT id, nome FROM materias WHERE id = :id";
        return namedParameterJdbcTemplate.queryForObject(sqlSelect,
            new MapSqlParameterSource().addValue("id", id), materiaMapper);
    }
}

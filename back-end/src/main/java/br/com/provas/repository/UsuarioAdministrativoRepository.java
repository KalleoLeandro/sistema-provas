package br.com.provas.repository;

import br.com.provas.model.entity.UsuarioAdministrativo;
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
public class UsuarioAdministrativoRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final RowMapper<UsuarioAdministrativo> usuarioAdministrativoMapper = (rs, rowNum) ->
        new UsuarioAdministrativo(rs.getLong("id"), rs.getString("nome"), rs.getString("login"),
            rs.getString("senha_hash"), rs.getBoolean("ativo"));

    public Optional<UsuarioAdministrativo> findByLogin(String login) {
        String sql = "SELECT id, nome, login, senha_hash, ativo FROM usuarios_administrativos WHERE login = :login";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("login", login);
        return namedParameterJdbcTemplate.query(sql, params, usuarioAdministrativoMapper).stream().findFirst();
    }

    public boolean isActiveMaster(Long id) {
        String sql = "SELECT COUNT(*) FROM usuarios_administrativos WHERE id = :id AND master = TRUE AND ativo = TRUE";
        Long quantidade = namedParameterJdbcTemplate.queryForObject(sql,
            new MapSqlParameterSource().addValue("id", id), Long.class);
        return quantidade != null && quantidade > 0;
    }

    public List<UsuarioAdministrativo> findAllVisibleTo(Long usuarioId) {
        String sql = "SELECT id, nome, login, senha_hash, ativo FROM usuarios_administrativos WHERE master = FALSE OR id = :usuario_id ORDER BY nome, id";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource().addValue("usuario_id", usuarioId), usuarioAdministrativoMapper);
    }

    public Optional<UsuarioAdministrativo> findById(Long id) {
        String sql = "SELECT id, nome, login, senha_hash, ativo FROM usuarios_administrativos WHERE id = :id";
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource().addValue("id", id),
            usuarioAdministrativoMapper).stream().findFirst();
    }

    public void updateStatus(Long id, boolean ativo) {
        String sql = "UPDATE usuarios_administrativos SET ativo = :ativo WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id).addValue("ativo", ativo);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void update(Long id, String login, String senhaHash) {
        String sql = "UPDATE usuarios_administrativos SET login = :login, senha_hash = :senha_hash WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id)
            .addValue("login", login).addValue("senha_hash", senhaHash);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public UsuarioAdministrativo save(String login, String senhaHash) {
        String sql = "INSERT INTO usuarios_administrativos (nome, login, senha_hash) VALUES (:nome, :login, :senha_hash)";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("nome", login)
            .addValue("login", login)
            .addValue("senha_hash", senhaHash);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });
        long id = Objects.requireNonNull(keyHolder.getKey(), "ID não retornado pelo banco.").longValue();
        String sqlSelect = "SELECT id, nome, login, senha_hash, ativo FROM usuarios_administrativos WHERE id = :id";
        return namedParameterJdbcTemplate.queryForObject(sqlSelect,
            new MapSqlParameterSource().addValue("id", id), usuarioAdministrativoMapper);
    }
}

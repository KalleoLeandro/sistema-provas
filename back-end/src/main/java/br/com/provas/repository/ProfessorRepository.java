package br.com.provas.repository;

import br.com.provas.model.entity.Professor;
import br.com.provas.model.entity.Turma;
import br.com.provas.model.entity.ProfessorMateria;
import br.com.provas.model.request.ProfessorMateriaRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProfessorRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Professor> professorMapper = (rs, rowNum) -> new Professor(
        rs.getLong("id"), rs.getString("nome"), rs.getString("login"), rs.getString("senha_hash"), rs.getBoolean("ativo"));

    public List<Professor> findAll() {
        return namedParameterJdbcTemplate.query("SELECT id, nome, login, senha_hash, ativo FROM professores ORDER BY nome, id",
            new MapSqlParameterSource(), professorMapper);
    }

    public Optional<Professor> findById(Long id) {
        return namedParameterJdbcTemplate.query("SELECT id, nome, login, senha_hash, ativo FROM professores WHERE id = :id",
            new MapSqlParameterSource().addValue("id", id), professorMapper).stream().findFirst();
    }

    public Optional<Professor> findByLogin(String login) {
        return namedParameterJdbcTemplate.query("SELECT id, nome, login, senha_hash, ativo FROM professores WHERE login = :login",
            new MapSqlParameterSource().addValue("login", login), professorMapper).stream().findFirst();
    }

    public Optional<Professor> findByIdForUpdate(Long id) {
        return namedParameterJdbcTemplate.query("SELECT id, nome, login, senha_hash, ativo FROM professores WHERE id = :id FOR UPDATE",
            new MapSqlParameterSource().addValue("id", id), professorMapper).stream().findFirst();
    }

    public List<Long> findExistingMateriaIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return namedParameterJdbcTemplate.query("SELECT id FROM materias WHERE id IN (:ids) FOR SHARE",
            new MapSqlParameterSource().addValue("ids", ids), (rs, rowNum) -> rs.getLong("id"));
    }

    public void saveMaterias(Long professorId, List<ProfessorMateriaRequest> materias, boolean ativo) {
        var params = materias.stream().map(m -> new MapSqlParameterSource()
            .addValue("professor_id", professorId).addValue("materia_id", m.materiaId())).toArray(MapSqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate("INSERT INTO professores_materias (professor_id, materia_id) VALUES (:professor_id, :materia_id)", params);
        var vinculos = materias.stream().sorted(Comparator.comparing(ProfessorMateriaRequest::materiaId))
            .flatMap(m -> m.turmas().stream().sorted().map(id -> new MapSqlParameterSource()
                .addValue("professor_id", professorId).addValue("materia_id", m.materiaId())
                .addValue("turma_id", id).addValue("ativo", ativo))).toArray(MapSqlParameterSource[]::new);
        if (vinculos.length > 0) namedParameterJdbcTemplate.batchUpdate("""
            INSERT INTO professores_materias_turmas (professor_id, materia_id, turma_id, ativo)
            VALUES (:professor_id, :materia_id, :turma_id, :ativo)
            """, vinculos);
    }

    public void updateMateriasStatus(Long professorId, boolean ativo) {
        namedParameterJdbcTemplate.update("UPDATE professores_materias_turmas SET ativo = :ativo WHERE professor_id = :id ORDER BY materia_id, turma_id",
            new MapSqlParameterSource().addValue("id", professorId).addValue("ativo", ativo));
    }

    public Map<Long, List<ProfessorMateria>> findMateriasByProfessorIds(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        record Vinculo(Long professorId, Long materiaId, String nome, Turma turma) {}
        RowMapper<Vinculo> mapper = (rs, rowNum) -> {
            Long turmaId = rs.getObject("turma_id", Long.class);
            return new Vinculo(rs.getLong("professor_id"), rs.getLong("materia_id"), rs.getString("nome"),
                turmaId == null ? null : new Turma(turmaId, rs.getString("ensino"), rs.getInt("ano"), rs.getString("letra")));
        };
        var vinculos = namedParameterJdbcTemplate.query("""
            SELECT pm.professor_id, pm.materia_id, m.nome, t.id AS turma_id, t.ensino, t.ano, t.letra
            FROM professores_materias pm JOIN materias m ON m.id = pm.materia_id
            LEFT JOIN professores_materias_turmas pmt ON pmt.professor_id = pm.professor_id AND pmt.materia_id = pm.materia_id
            LEFT JOIN turmas t ON t.id = pmt.turma_id
            WHERE pm.professor_id IN (:ids) ORDER BY m.nome, m.id, t.ensino, t.ano, t.letra
            """, new MapSqlParameterSource().addValue("ids", ids), mapper);
        Map<Long, Map<Long, ProfessorMateria>> agrupados = new LinkedHashMap<>();
        for (var v : vinculos) {
            var materia = agrupados.computeIfAbsent(v.professorId(), key -> new LinkedHashMap<>())
                .computeIfAbsent(v.materiaId(), key -> new ProfessorMateria(v.materiaId(), v.nome(), new ArrayList<>()));
            if (v.turma() != null) materia.turmas().add(v.turma());
        }
        Map<Long, List<ProfessorMateria>> resultado = new HashMap<>();
        agrupados.forEach((id, materias) -> resultado.put(id, List.copyOf(materias.values())));
        return resultado;
    }

    public Professor save(String nome, String login, String senhaHash) {
        var params = new MapSqlParameterSource().addValue("nome", nome).addValue("login", login)
            .addValue("senha_hash", senhaHash);
        var keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("INSERT INTO professores (nome, login, senha_hash) VALUES (:nome, :login, :senha_hash)",
            params, keyHolder, new String[] { "id" });
        return findById(Objects.requireNonNull(keyHolder.getKey(), "ID não retornado pelo banco.").longValue()).orElseThrow();
    }

    public List<Long> findExistingTurmaIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return namedParameterJdbcTemplate.query("SELECT id FROM turmas WHERE id IN (:ids) FOR SHARE",
            new MapSqlParameterSource().addValue("ids", ids), (rs, rowNum) -> rs.getLong("id"));
    }

    public void saveTurmas(Long professorId, List<Long> turmas) {
        if (turmas.isEmpty()) return;
        var params = turmas.stream().map(id -> new MapSqlParameterSource()
            .addValue("professor_id", professorId).addValue("turma_id", id)).toArray(MapSqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate("INSERT INTO professores_turmas (professor_id, turma_id) VALUES (:professor_id, :turma_id)", params);
    }

    public Map<Long, List<Turma>> findTurmasByProfessorIds(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        record Vinculo(Long professorId, Turma turma) {}
        RowMapper<Vinculo> mapper = (rs, rowNum) -> new Vinculo(rs.getLong("professor_id"), new Turma(
            rs.getLong("id"), rs.getString("ensino"), rs.getInt("ano"), rs.getString("letra")));
        var vinculos = namedParameterJdbcTemplate.query("""
            SELECT pt.professor_id, t.id, t.ensino, t.ano, t.letra
            FROM professores_turmas pt JOIN turmas t ON t.id = pt.turma_id
            WHERE pt.professor_id IN (:ids) ORDER BY t.ensino, t.ano, t.letra
            """, new MapSqlParameterSource().addValue("ids", ids), mapper);
        Map<Long, List<Turma>> resultado = new HashMap<>();
        vinculos.forEach(v -> resultado.computeIfAbsent(v.professorId(), key -> new ArrayList<>()).add(v.turma()));
        return resultado;
    }

    public void updateStatus(Long id, boolean ativo) {
        namedParameterJdbcTemplate.update("UPDATE professores SET ativo = :ativo WHERE id = :id",
            new MapSqlParameterSource().addValue("id", id).addValue("ativo", ativo));
    }
    public void update(Long id,String nome,String login,String senhaHash){
        namedParameterJdbcTemplate.update("UPDATE professores SET nome=:nome,login=:login,senha_hash=:senha WHERE id=:id",
            new MapSqlParameterSource().addValue("id",id).addValue("nome",nome).addValue("login",login).addValue("senha",senhaHash));
    }
    public void replaceMaterias(Long id,List<ProfessorMateriaRequest> materias,boolean ativo){
        var params=new MapSqlParameterSource().addValue("id",id);
        namedParameterJdbcTemplate.update("DELETE FROM professores_materias WHERE professor_id=:id",params);
        namedParameterJdbcTemplate.update("DELETE FROM professores_turmas WHERE professor_id=:id",params);
        saveMaterias(id,materias,ativo);
    }
}

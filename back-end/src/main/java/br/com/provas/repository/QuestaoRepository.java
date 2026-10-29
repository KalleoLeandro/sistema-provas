package br.com.provas.repository;

import br.com.provas.model.entity.Questao;
import br.com.provas.model.entity.QuantidadeQuestoesTurma;
import br.com.provas.model.entity.QuestaoResumo;
import br.com.provas.model.entity.QuestaoDestino;
import br.com.provas.model.entity.Turma;
import br.com.provas.model.request.QuestaoRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestaoRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<Questao> questaoMapper = (rs, rowNum) -> {
        Map<String, String> alternativas = new LinkedHashMap<>();
        for (String letra : List.of("A", "B", "C", "D", "E")) {
            String html = rs.getString("alternativa_" + letra.toLowerCase(Locale.ROOT));
            if (html != null) alternativas.put(letra, html);
        }
        return new Questao(rs.getLong("id"), rs.getLong("professor_id"), rs.getLong("materia_id"), rs.getString("ensino"), rs.getInt("ano"), rs.getString("abrangencia"),
            rs.getString("tipo"), rs.getBigDecimal("peso"), rs.getString("enunciado"), alternativas, rs.getString("resposta_correta"),rs.getLong("assunto_id"));
    };

    public boolean hasSubject(Long assunto,Long materia){return !namedParameterJdbcTemplate.query("SELECT id FROM assuntos WHERE id=:assunto AND materia_id=:materia AND nome <> 'Geral' FOR SHARE",new MapSqlParameterSource().addValue("assunto",assunto).addValue("materia",materia),(rs,n)->rs.getLong("id")).isEmpty();}
    public String subjectName(Long id){if(id==null)return null;return namedParameterJdbcTemplate.query("SELECT nome FROM assuntos WHERE id=:id",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getString("nome")).stream().findFirst().orElse(null);}
    public boolean hasActiveYear(Long professorId, Long materiaId, String ensino, Integer ano) {
        String sql = """
            SELECT pmt.professor_id FROM professores_materias_turmas pmt
            JOIN professores p ON p.id = pmt.professor_id JOIN turmas t ON t.id = pmt.turma_id
            WHERE pmt.professor_id = :professor_id AND pmt.materia_id = :materia_id AND t.ensino = :ensino AND t.ano = :ano
              AND p.ativo = TRUE AND pmt.ativo = TRUE FOR SHARE
            """;
        var params = new MapSqlParameterSource().addValue("professor_id", professorId).addValue("materia_id", materiaId)
            .addValue("ensino", ensino).addValue("ano", ano);
        return !namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getLong("professor_id")).isEmpty();
    }

    private final RowMapper<Turma> turmaMapper = (rs, rowNum) -> new Turma(rs.getLong("id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"));

    public List<Turma> findTurmasByIds(List<Long> ids, Long professorId, Long materiaId) {
        if (ids.isEmpty()) return List.of();
        String sql = """
            SELECT t.id,t.ensino,t.ano,t.letra FROM turmas t
            JOIN professores_materias_turmas pmt ON pmt.turma_id=t.id
            JOIN professores p ON p.id=pmt.professor_id
            WHERE t.id IN (:ids) AND pmt.professor_id=:professor_id AND pmt.materia_id=:materia_id
              AND pmt.ativo=TRUE AND p.ativo=TRUE ORDER BY t.id FOR SHARE
            """;
        return namedParameterJdbcTemplate.query(sql,new MapSqlParameterSource().addValue("ids",ids)
            .addValue("professor_id",professorId).addValue("materia_id",materiaId),turmaMapper);
    }

    public List<Turma> findTargets(Questao questao) {
        if ("ano".equals(questao.abrangencia())) {
            return namedParameterJdbcTemplate.query("""
                SELECT t.id,t.ensino,t.ano,t.letra FROM turmas t
                JOIN professores_materias_turmas pmt ON pmt.turma_id=t.id
                JOIN professores p ON p.id=pmt.professor_id
                WHERE t.ensino=:ensino AND t.ano=:ano AND pmt.professor_id=:professor_id
                  AND pmt.materia_id=:materia_id AND pmt.ativo=TRUE AND p.ativo=TRUE ORDER BY t.letra
                """,
                new MapSqlParameterSource().addValue("ensino",questao.ensino()).addValue("ano",questao.ano())
                    .addValue("professor_id",questao.professorId()).addValue("materia_id",questao.materiaId()),turmaMapper);
        }
        return namedParameterJdbcTemplate.query("SELECT t.id, t.ensino, t.ano, t.letra FROM questoes_turmas qt JOIN turmas t ON t.id = qt.turma_id WHERE qt.questao_id = :id ORDER BY t.letra",
            new MapSqlParameterSource().addValue("id", questao.id()), turmaMapper);
    }

    public List<QuestaoDestino> findOptions(Long professorId) {
        String sql = """
            SELECT DISTINCT m.id AS materia_id, m.nome AS materia, t.id AS turma_id, t.ensino, t.ano, t.letra
            FROM professores_materias_turmas pmt JOIN professores p ON p.id = pmt.professor_id
            JOIN materias m ON m.id = pmt.materia_id JOIN turmas t ON t.id = pmt.turma_id
            WHERE pmt.professor_id = :professor_id AND p.ativo = TRUE AND pmt.ativo = TRUE
            ORDER BY m.nome, t.ensino, t.ano, t.letra
            """;
        RowMapper<QuestaoDestino> mapper = (rs,rowNum) -> new QuestaoDestino(rs.getLong("materia_id"), rs.getString("materia"),
            rs.getLong("turma_id"), rs.getString("ensino"), rs.getInt("ano"), rs.getString("letra"));
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource().addValue("professor_id", professorId), mapper);
    }

    public Questao save(Long professorId, QuestaoRequest request) {
        var params = new MapSqlParameterSource().addValue("professor_id", professorId).addValue("assunto_id",request.assuntoId()).addValue("materia_id", request.materiaId())
            .addValue("ensino", request.ensino()).addValue("ano", request.ano()).addValue("abrangencia", request.abrangencia()).addValue("tipo", request.tipo()).addValue("peso", request.peso())
            .addValue("enunciado", request.enunciado()).addValue("resposta_correta", request.respostaCorreta());
        for (String letra : List.of("A", "B", "C", "D", "E")) {
            params.addValue("alternativa_" + letra.toLowerCase(Locale.ROOT), request.alternativas().get(letra));
        }
        String sql = """
            INSERT INTO questoes (professor_id, assunto_id, materia_id, ensino, ano, abrangencia, tipo, peso, enunciado,
              alternativa_a, alternativa_b, alternativa_c, alternativa_d, alternativa_e, resposta_correta)
            VALUES (:professor_id, :assunto_id, :materia_id, :ensino, :ano, :abrangencia, :tipo, :peso, :enunciado,
              :alternativa_a, :alternativa_b, :alternativa_c, :alternativa_d, :alternativa_e, :resposta_correta)
            """;
        var keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] { "id" });
        Long id = Objects.requireNonNull(keyHolder.getKey(), "ID não retornado pelo banco.").longValue();
        if ("turmas".equals(request.abrangencia())) {
            var vinculos = request.turmas().stream().sorted().map(turmaId -> new MapSqlParameterSource().addValue("questao_id", id).addValue("turma_id", turmaId)).toArray(MapSqlParameterSource[]::new);
            namedParameterJdbcTemplate.batchUpdate("INSERT INTO questoes_turmas (questao_id, turma_id) VALUES (:questao_id, :turma_id)", vinculos);
        }
        return findById(id, professorId).orElseThrow();
    }

    public Optional<Questao> findByIdForUpdate(Long id, Long professorId) {
        return namedParameterJdbcTemplate.query("SELECT * FROM questoes WHERE id = :id AND professor_id = :professor_id FOR UPDATE",
            new MapSqlParameterSource().addValue("id", id).addValue("professor_id", professorId), questaoMapper).stream().findFirst();
    }

    public Long findCurrentVersionId(Long id) {
        return namedParameterJdbcTemplate.query("SELECT id FROM questoes_versoes WHERE questao_id = :id ORDER BY numero DESC LIMIT 1",
            new MapSqlParameterSource().addValue("id", id), (rs, rowNum) -> rs.getLong("id")).stream().findFirst().orElse(null);
    }

    public Long findVersionRevision(Long versaoId) {
        return namedParameterJdbcTemplate.query("SELECT revisao FROM questoes_versoes WHERE id=:id",
            new MapSqlParameterSource().addValue("id",versaoId), (rs,n) -> rs.getLong("revisao")).stream().findFirst().orElse(null);
    }

    public boolean isVersionUsedInAnsweredExam(Long versaoId) {
        return namedParameterJdbcTemplate.query("SELECT utilizada_em_prova_respondida FROM questoes_versoes WHERE id=:id",
            new MapSqlParameterSource().addValue("id",versaoId), (rs,n) -> rs.getBoolean("utilizada_em_prova_respondida")).stream().findFirst().orElse(false);
    }

    public void updateUnusedVersion(Long id, Long versaoId) {
        String sql = """
            UPDATE questoes_versoes v JOIN questoes q ON q.id=v.questao_id
            SET v.assunto_id=q.assunto_id, v.materia_id=q.materia_id, v.ensino=q.ensino, v.ano=q.ano, v.abrangencia=q.abrangencia,
              v.turmas=COALESCE((SELECT JSON_ARRAYAGG(qt.turma_id) FROM questoes_turmas qt WHERE qt.questao_id=q.id), JSON_ARRAY()),
              v.tipo=q.tipo, v.peso=q.peso, v.enunciado=q.enunciado, v.alternativa_a=q.alternativa_a,
              v.alternativa_b=q.alternativa_b, v.alternativa_c=q.alternativa_c, v.alternativa_d=q.alternativa_d,
              v.alternativa_e=q.alternativa_e, v.resposta_correta=q.resposta_correta, v.revisao=v.revisao+1
            WHERE q.id=:id AND v.id=:versao_id AND v.utilizada_em_prova_respondida=FALSE
            """;
        int updated = namedParameterJdbcTemplate.update(sql,new MapSqlParameterSource().addValue("id",id).addValue("versao_id",versaoId));
        if (updated != 1) throw new IllegalStateException("A versão não pode ser alterada.");
    }

    public void saveVersion(Long id) {
        String sql = """
            INSERT INTO questoes_versoes (questao_id, numero, assunto_id, materia_id, ensino, ano, abrangencia, turmas, tipo, peso, enunciado,
              alternativa_a, alternativa_b, alternativa_c, alternativa_d, alternativa_e, resposta_correta)
            SELECT q.id, (SELECT COALESCE(MAX(v.numero),0)+1 FROM questoes_versoes v WHERE v.questao_id=q.id),
              q.assunto_id, q.materia_id, q.ensino, q.ano, q.abrangencia,
              COALESCE((SELECT JSON_ARRAYAGG(qt.turma_id) FROM questoes_turmas qt WHERE qt.questao_id=q.id), JSON_ARRAY()),
              q.tipo, q.peso, q.enunciado, q.alternativa_a, q.alternativa_b, q.alternativa_c, q.alternativa_d, q.alternativa_e, q.resposta_correta
            FROM questoes q WHERE q.id=:id
            """;
        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource().addValue("id", id));
    }

    public Questao update(Long id, Long professorId, QuestaoRequest request) {
        var params = new MapSqlParameterSource().addValue("id",id).addValue("professor_id",professorId)
            .addValue("assunto_id",request.assuntoId()).addValue("materia_id",request.materiaId()).addValue("ensino",request.ensino()).addValue("ano",request.ano())
            .addValue("abrangencia",request.abrangencia()).addValue("tipo",request.tipo()).addValue("peso",request.peso())
            .addValue("enunciado",request.enunciado()).addValue("resposta_correta",request.respostaCorreta());
        for (String letra : List.of("A","B","C","D","E")) params.addValue("alternativa_"+letra.toLowerCase(Locale.ROOT), request.alternativas().get(letra));
        namedParameterJdbcTemplate.update("""
            UPDATE questoes SET assunto_id=:assunto_id, materia_id=:materia_id, ensino=:ensino, ano=:ano, abrangencia=:abrangencia,
              tipo=:tipo, peso=:peso, enunciado=:enunciado, alternativa_a=:alternativa_a, alternativa_b=:alternativa_b,
              alternativa_c=:alternativa_c, alternativa_d=:alternativa_d, alternativa_e=:alternativa_e, resposta_correta=:resposta_correta
            WHERE id=:id AND professor_id=:professor_id
            """, params);
        namedParameterJdbcTemplate.update("DELETE FROM questoes_turmas WHERE questao_id=:id", new MapSqlParameterSource().addValue("id",id));
        if ("turmas".equals(request.abrangencia())) {
            var vinculos = request.turmas().stream().sorted().map(t -> new MapSqlParameterSource().addValue("id",id).addValue("turma_id",t)).toArray(MapSqlParameterSource[]::new);
            namedParameterJdbcTemplate.batchUpdate("INSERT INTO questoes_turmas (questao_id,turma_id) VALUES (:id,:turma_id)",vinculos);
        }
        return findById(id,professorId).orElseThrow();
    }

    public Optional<Questao> findById(Long id, Long professorId) {
        return namedParameterJdbcTemplate.query("SELECT * FROM questoes WHERE id = :id AND professor_id = :professor_id",
            new MapSqlParameterSource().addValue("id", id).addValue("professor_id", professorId), questaoMapper).stream().findFirst();
    }

    public List<QuestaoResumo> findAll(Long professorId) {
        String sql = """
            SELECT q.id, q.assunto_id, a.nome AS assunto, m.nome AS materia, q.ensino, q.ano, q.abrangencia, q.tipo, q.peso,
              (SELECT GROUP_CONCAT(t.letra ORDER BY t.letra SEPARATOR ', ') FROM questoes_turmas qt JOIN turmas t ON t.id = qt.turma_id WHERE qt.questao_id = q.id) AS letras,
              LEFT(REGEXP_REPLACE(REGEXP_REPLACE(q.enunciado, '<(script|style|svg)[^>]*>.*?</(script|style|svg)>|<!--.*?-->', ' ', 1, 0, 'in'), '<[^>]*>', ' '), 1024) AS enunciado
            FROM questoes q JOIN assuntos a ON a.id=q.assunto_id JOIN materias m ON m.id = q.materia_id
            WHERE q.professor_id = :professor_id ORDER BY q.id DESC
            """;
        RowMapper<QuestaoResumo> mapper = (rs, rowNum) -> new QuestaoResumo(rs.getLong("id"), rs.getString("materia"),
            rs.getString("ensino"), rs.getInt("ano"), rs.getString("letras"), rs.getString("abrangencia"), rs.getString("tipo"), rs.getBigDecimal("peso"), rs.getString("enunciado"),rs.getLong("assunto_id"),rs.getString("assunto"));
        return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource().addValue("professor_id", professorId), mapper);
    }
    public List<br.com.provas.model.entity.PendenciaComposicao> countMissingPlans(Long professorId){
        String sql="""
            SELECT l.id AS liberacao_id,l.titulo,m.id AS materia_id,m.nome AS materia,t.id AS turma_id,t.ensino,t.ano,t.letra,
              a.id AS assunto_id,a.nome AS assunto,c.peso,c.quantidade AS minimo,COUNT(q.id) AS cadastradas
            FROM provas_liberacoes l
            JOIN professores_materias_turmas pmt ON pmt.professor_id=l.professor_id AND pmt.materia_id=l.materia_id AND pmt.turma_id=l.turma_id AND pmt.ativo=TRUE
            JOIN professores p ON p.id=l.professor_id AND p.ativo=TRUE
            JOIN materias m ON m.id=l.materia_id JOIN turmas t ON t.id=l.turma_id
            JOIN JSON_TABLE(COALESCE(l.composicao,JSON_ARRAY()),'$[*]' COLUMNS(assunto_id BIGINT PATH '$.assuntoId',peso DECIMAL(8,2) PATH '$.peso',quantidade INT PATH '$.quantidade')) c
            JOIN assuntos a ON a.id=c.assunto_id
            LEFT JOIN questoes q ON q.professor_id=l.professor_id AND q.materia_id=l.materia_id AND (a.nome='Geral' OR q.assunto_id=c.assunto_id) AND q.peso=c.peso AND q.ensino=t.ensino AND q.ano=t.ano
              AND (q.abrangencia='ano' OR EXISTS(SELECT 1 FROM questoes_turmas qt WHERE qt.questao_id=q.id AND qt.turma_id=t.id))
            WHERE l.professor_id=:professor_id
            GROUP BY l.id,l.titulo,m.id,m.nome,t.id,t.ensino,t.ano,t.letra,a.id,a.nome,c.peso,c.quantidade
            HAVING COUNT(q.id)<c.quantidade ORDER BY l.id,a.nome,c.peso
            """;
        return namedParameterJdbcTemplate.query(sql,new MapSqlParameterSource().addValue("professor_id",professorId),(rs,n)->new br.com.provas.model.entity.PendenciaComposicao(rs.getLong("liberacao_id"),rs.getString("titulo"),rs.getLong("materia_id"),rs.getString("materia"),rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"),rs.getLong("assunto_id"),rs.getString("assunto"),rs.getBigDecimal("peso"),rs.getInt("minimo"),rs.getInt("cadastradas")));
    }
}

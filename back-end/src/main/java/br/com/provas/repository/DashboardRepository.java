package br.com.provas.repository;

import br.com.provas.model.response.DashboardIndicadoresResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private static final String BASE = """
        WITH notas AS (
          SELECT tentativa_id,COALESCE(SUM(pontuacao),0) AS nota,
            SUM(CASE WHEN tipo='discursiva' AND NULLIF(TRIM(resposta),'') IS NOT NULL AND pontuacao IS NULL THEN 1 ELSE 0 END) AS pendentes
          FROM provas_tentativas_questoes GROUP BY tentativa_id
        ), avaliacoes AS (
          SELECT a.id AS aluno_id,l.turma_id,t.ensino,t.ano,t.letra,pt.id AS tentativa_id,pt.submetida_em,
            COALESCE(n.pendentes,0) AS pendentes,COALESCE(n.nota,0) AS nota
          FROM provas_liberacoes l JOIN turmas t ON t.id=l.turma_id
          JOIN alunos_2026 a ON a.turma_id=l.turma_id AND a.status='M'
          LEFT JOIN provas_tentativas pt ON pt.liberacao_id=l.id AND pt.aluno_id=a.id
          LEFT JOIN notas n ON n.tentativa_id=pt.id
          WHERE l.disponivel_em<=:agora AND (:professor IS NULL OR (l.professor_id=:professor AND EXISTS (
            SELECT 1 FROM professores_materias_turmas pmt JOIN professores p ON p.id=pmt.professor_id AND p.ativo=TRUE
            WHERE pmt.professor_id=:professor AND pmt.turma_id=l.turma_id AND pmt.materia_id=l.materia_id AND pmt.ativo=TRUE)))
        )
        """;
    private final RowMapper<DashboardIndicadoresResponse> resumoMapper=(rs,n)->new DashboardIndicadoresResponse(
        rs.getLong("alunos_fizeram"),rs.getLong("alunos_pendentes"),rs.getBigDecimal("media"),
        rs.getLong("nao_iniciadas"),rs.getLong("em_andamento"),rs.getLong("aguardando"),rs.getLong("corrigidas"),List.of());
    private final RowMapper<DashboardIndicadoresResponse.MediaTurma> turmaMapper=(rs,n)->new DashboardIndicadoresResponse.MediaTurma(
        rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"),rs.getBigDecimal("media"),rs.getLong("corrigidas"));
    private MapSqlParameterSource params(Long professor,Instant agora){return new MapSqlParameterSource().addValue("professor",professor).addValue("agora",LocalDateTime.ofInstant(agora,ZoneOffset.UTC));}
    public DashboardIndicadoresResponse resumo(Long professor,Instant agora){return jdbc.query(BASE+"""
        SELECT COUNT(DISTINCT CASE WHEN submetida_em IS NOT NULL THEN aluno_id END) AS alunos_fizeram,
          COUNT(DISTINCT CASE WHEN submetida_em IS NULL THEN aluno_id END) AS alunos_pendentes,
          AVG(CASE WHEN submetida_em IS NOT NULL AND pendentes=0 THEN nota END) AS media,
          COALESCE(SUM(tentativa_id IS NULL),0) AS nao_iniciadas,
          COALESCE(SUM(tentativa_id IS NOT NULL AND submetida_em IS NULL),0) AS em_andamento,
          COALESCE(SUM(submetida_em IS NOT NULL AND pendentes>0),0) AS aguardando,
          COALESCE(SUM(submetida_em IS NOT NULL AND pendentes=0),0) AS corrigidas
        FROM avaliacoes
        """,params(professor,agora),resumoMapper).getFirst();}
    public List<DashboardIndicadoresResponse.MediaTurma> medias(Long professor,Instant agora){return jdbc.query(BASE+"""
        SELECT turma_id,ensino,ano,letra,AVG(CASE WHEN submetida_em IS NOT NULL AND pendentes=0 THEN nota END) AS media,
          SUM(submetida_em IS NOT NULL AND pendentes=0) AS corrigidas
        FROM avaliacoes GROUP BY turma_id,ensino,ano,letra ORDER BY ensino,ano,letra,turma_id
        """,params(professor,agora),turmaMapper);}
}

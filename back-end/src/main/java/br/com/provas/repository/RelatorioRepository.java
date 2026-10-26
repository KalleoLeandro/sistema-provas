package br.com.provas.repository;

import br.com.provas.model.entity.Turma;
import br.com.provas.model.response.AlunoResponse;
import br.com.provas.model.response.RelatorioProvaResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RelatorioRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private static final String ACESSO_TURMA = """
        (:professor IS NULL OR EXISTS (SELECT 1 FROM professores_materias_turmas pmt
        JOIN professores p ON p.id=pmt.professor_id AND p.ativo=TRUE
        WHERE pmt.professor_id=:professor AND pmt.turma_id=t.id AND pmt.ativo=TRUE))
        """;
    private static final String PROVAS = """
        SELECT l.id,l.titulo,m.nome AS materia,p.nome AS professor,l.turma_id,l.disponivel_em,l.encerra_em
        FROM provas_liberacoes l JOIN materias m ON m.id=l.materia_id JOIN professores p ON p.id=l.professor_id
        WHERE (:professor IS NULL OR l.professor_id=:professor)
        """;
    private final RowMapper<Turma> turmaMapper=(rs,n)->new Turma(rs.getLong("id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"));
    private final RowMapper<AlunoResponse> alunoMapper=(rs,n)->new AlunoResponse(rs.getLong("id"),rs.getString("nome"),rs.getObject("numero",Integer.class),rs.getString("cgm"),rs.getString("status"),rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"));
    private final RowMapper<RelatorioProvaResponse> provaMapper=(rs,n)->new RelatorioProvaResponse(rs.getLong("id"),rs.getString("titulo"),rs.getString("materia"),rs.getString("professor"),rs.getLong("turma_id"),instant(rs,"disponivel_em"),instant(rs,"encerra_em"));
    private final RowMapper<RelatorioProvaResponse.Resultado> resultadoMapper=(rs,n)->new RelatorioProvaResponse.Resultado(rs.getLong("aluno_id"),rs.getString("nome"),rs.getObject("numero",Integer.class),rs.getString("cgm"),rs.getString("status"),rs.getObject("tentativa_id",Long.class),instant(rs,"iniciada_em"),instant(rs,"submetida_em"),rs.getInt("pendentes"),rs.getBigDecimal("nota"));
    private static Instant instant(java.sql.ResultSet rs,String campo)throws java.sql.SQLException {
        var data=rs.getObject(campo,LocalDateTime.class);return data==null?null:data.toInstant(ZoneOffset.UTC);
    }
    private MapSqlParameterSource params(Long professor){return new MapSqlParameterSource().addValue("professor",professor);}
    public List<Turma> turmas(Long professor){return jdbc.query("SELECT t.id,t.ensino,t.ano,t.letra FROM turmas t WHERE "+ACESSO_TURMA+" ORDER BY t.ensino,t.ano,t.letra",params(professor),turmaMapper);}
    public boolean turmaPermitida(Long professor,Long turma){return !jdbc.query("SELECT t.id,t.ensino,t.ano,t.letra FROM turmas t WHERE t.id=:turma AND "+ACESSO_TURMA,params(professor).addValue("turma",turma),turmaMapper).isEmpty();}
    public List<AlunoResponse> alunos(Long professor,Long turma){return jdbc.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id WHERE t.id=:turma AND "+ACESSO_TURMA+" ORDER BY a.numero IS NULL,a.numero,a.nome,a.id",params(professor).addValue("turma",turma),alunoMapper);}
    public List<RelatorioProvaResponse> provas(Long professor,Long turma){return jdbc.query(PROVAS+" AND l.turma_id=:turma ORDER BY l.disponivel_em DESC,l.id DESC",params(professor).addValue("turma",turma),provaMapper);}
    public Optional<RelatorioProvaResponse> prova(Long professor,Long id){return jdbc.query(PROVAS+" AND l.id=:id",params(professor).addValue("id",id),provaMapper).stream().findFirst();}
    public List<RelatorioProvaResponse.Resultado> resultados(Long professor,Long id){return jdbc.query("""
        SELECT a.id AS aluno_id,a.nome,a.numero,a.cgm,a.status,pt.id AS tentativa_id,pt.iniciada_em,pt.submetida_em,
          (SELECT COUNT(*) FROM provas_tentativas_questoes q WHERE q.tentativa_id=pt.id
            AND q.tipo='discursiva' AND NULLIF(TRIM(q.resposta),'') IS NOT NULL AND q.pontuacao IS NULL) AS pendentes,
          CASE WHEN pt.submetida_em IS NOT NULL THEN
            (SELECT COALESCE(SUM(q.pontuacao),0) FROM provas_tentativas_questoes q WHERE q.tentativa_id=pt.id) END AS nota
        FROM provas_liberacoes l JOIN alunos_2026 a ON a.turma_id=l.turma_id
          OR EXISTS (SELECT 1 FROM provas_tentativas historico WHERE historico.liberacao_id=l.id AND historico.aluno_id=a.id)
        LEFT JOIN provas_tentativas pt ON pt.liberacao_id=l.id AND pt.aluno_id=a.id
        WHERE l.id=:id AND (:professor IS NULL OR l.professor_id=:professor)
        ORDER BY a.numero IS NULL,a.numero,a.nome,a.id
        """,params(professor).addValue("id",id),resultadoMapper);}
}

package br.com.provas.repository;

import br.com.provas.model.response.AlunoResponse;
import java.util.List;
import java.util.Optional;
import br.com.provas.model.request.AlunoRequest;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AlunoRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String FILTRO = """
        FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id
        WHERE (:turma_id IS NULL OR a.turma_id=:turma_id)
          AND EXISTS (SELECT 1 FROM professores_materias_turmas pmt
            JOIN professores p ON p.id=pmt.professor_id AND p.ativo=TRUE
            WHERE pmt.professor_id=:professor_id AND pmt.ativo=TRUE AND pmt.turma_id=a.turma_id)
        """;
    private final RowMapper<AlunoResponse> mapper = (rs,n) -> new AlunoResponse(rs.getLong("id"),rs.getString("nome"),rs.getObject("numero",Integer.class),rs.getString("cgm"),
        rs.getString("status"),rs.getLong("turma_id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"));

    private static final String PESQUISA=" AND (:status IS NULL OR a.status=:status) AND (:busca='' OR LOCATE(:busca,a.nome)>0 OR LOCATE(:busca,a.cgm)>0) ";
    private MapSqlParameterSource filtros(Long professor,br.com.provas.model.request.AlunoFiltroRequest filtro){
        return new MapSqlParameterSource().addValue("professor_id",professor).addValue("turma_id",filtro.turmaId()).addValue("status",filtro.status()).addValue("busca",filtro.busca());
    }
    private String orderBy(br.com.provas.model.request.AlunoFiltroRequest filtro){
        // Apenas expressões conhecidas entram no SQL; nenhum identificador recebido é interpolado.
        String direction="DESC".equals(filtro.direcao())?" DESC":" ASC";
        String column=switch(filtro.ordenacao()){
            case "numero" -> "a.numero";case "cgm" -> "a.cgm";
            case "status" -> "CASE a.status WHEN 'D' THEN 'Desistente' WHEN 'M' THEN 'Matriculado' WHEN 'R' THEN 'Remanejado' ELSE 'Transferido' END";
            case "turma" -> "t.ensino"+direction+",t.ano"+direction+",t.letra";
            default -> "a.nome";
        };
        return " ORDER BY "+("numero".equals(filtro.ordenacao())?"a.numero IS NULL ASC,":"")+column+direction+",a.id"+direction;
    }
    public long countFiltered(Long professor,br.com.provas.model.request.AlunoFiltroRequest filtro){
        String from=professor==null?"FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id WHERE (:turma_id IS NULL OR a.turma_id=:turma_id) ":FILTRO;
        return namedParameterJdbcTemplate.query("SELECT COUNT(*) AS total "+from+PESQUISA,filtros(professor,filtro),(rs,n)->rs.getLong("total")).getFirst();
    }
    public List<AlunoResponse> findFiltered(Long professor,br.com.provas.model.request.AlunoFiltroRequest filtro,int tamanho,long offset){
        String from=professor==null?"FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id WHERE (:turma_id IS NULL OR a.turma_id=:turma_id) ":FILTRO;
        return namedParameterJdbcTemplate.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra "+from+PESQUISA+orderBy(filtro)+" LIMIT :tamanho OFFSET :offset",filtros(professor,filtro).addValue("tamanho",tamanho).addValue("offset",offset),mapper);
    }
    public long countByProfessor(Long professorId, Long turmaId) {
        var params = new MapSqlParameterSource().addValue("professor_id",professorId).addValue("turma_id",turmaId);
        return namedParameterJdbcTemplate.query("SELECT COUNT(*) AS total "+FILTRO,params,(rs,n)->rs.getLong("total")).getFirst();
    }
    public List<AlunoResponse> findByProfessor(Long professorId, Long turmaId, int tamanho, long offset) {
        var params = new MapSqlParameterSource().addValue("professor_id",professorId).addValue("turma_id",turmaId)
            .addValue("tamanho",tamanho).addValue("offset",offset);
        return namedParameterJdbcTemplate.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra "+FILTRO+
            " ORDER BY a.nome,a.id LIMIT :tamanho OFFSET :offset",params,mapper);
    }
    public boolean turmaExists(Long id) {
        return !namedParameterJdbcTemplate.query("SELECT id FROM turmas WHERE id=:id FOR SHARE",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getLong("id")).isEmpty();
    }
    public AlunoResponse save(AlunoRequest request) {
        var params=new MapSqlParameterSource().addValue("nome",request.nome()).addValue("numero",request.numero())
            .addValue("cgm",request.cgm()).addValue("status",request.status()).addValue("turma_id",request.turmaId());
        var key=new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update("INSERT INTO alunos_2026 (nome,numero,cgm,status,turma_id) VALUES (:nome,:numero,:cgm,:status,:turma_id)",params,key,new String[]{"id"});
        Long id=java.util.Objects.requireNonNull(key.getKey()).longValue();
        return findById(id).orElseThrow();
    }
    public Optional<AlunoResponse> findById(Long id) {
        return namedParameterJdbcTemplate.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id WHERE a.id=:id",
            new MapSqlParameterSource().addValue("id",id),mapper).stream().findFirst();
    }
    public long countAll() {
        return namedParameterJdbcTemplate.query("SELECT COUNT(*) AS total FROM alunos_2026",new MapSqlParameterSource(),(rs,n)->rs.getLong("total")).getFirst();
    }
    public boolean lockById(Long id){
        return !namedParameterJdbcTemplate.query("SELECT id FROM alunos_2026 WHERE id=:id FOR UPDATE",new MapSqlParameterSource().addValue("id",id),(rs,n)->rs.getLong("id")).isEmpty();
    }
    public void update(Long id,AlunoRequest request){
        namedParameterJdbcTemplate.update("UPDATE alunos_2026 SET nome=:nome,numero=:numero,cgm=:cgm,status=:status,turma_id=:turma WHERE id=:id",
            new MapSqlParameterSource().addValue("id",id).addValue("nome",request.nome()).addValue("numero",request.numero()).addValue("cgm",request.cgm()).addValue("status",request.status()).addValue("turma",request.turmaId()));
    }
    public List<AlunoResponse> findAll(int tamanho,long offset) {
        return namedParameterJdbcTemplate.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id ORDER BY a.nome,a.id LIMIT :tamanho OFFSET :offset",
            new MapSqlParameterSource().addValue("tamanho",tamanho).addValue("offset",offset),mapper);
    }

    public List<br.com.provas.model.entity.Turma> findImportTurmas() {
        RowMapper<br.com.provas.model.entity.Turma> turmaMapper=(rs,n)->new br.com.provas.model.entity.Turma(rs.getLong("id"),rs.getString("ensino"),rs.getInt("ano"),rs.getString("letra"));
        return namedParameterJdbcTemplate.query("SELECT id,ensino,ano,letra FROM turmas",new MapSqlParameterSource(),turmaMapper);
    }
    public java.util.Set<String> findExistingCgms(List<String> cgms) {
        if(cgms.isEmpty())return java.util.Set.of();
        return new java.util.HashSet<>(namedParameterJdbcTemplate.query("SELECT cgm FROM alunos_2026 WHERE cgm IN (:cgms)",
            new MapSqlParameterSource().addValue("cgms",cgms),(rs,n)->rs.getString("cgm")));
    }
    public void saveAll(List<AlunoRequest> alunos) {
        var params=alunos.stream().map(a->new MapSqlParameterSource().addValue("nome",a.nome()).addValue("numero",a.numero())
            .addValue("cgm",a.cgm()).addValue("status",a.status()).addValue("turma_id",a.turmaId())).toArray(MapSqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate("INSERT INTO alunos_2026(nome,numero,cgm,status,turma_id) VALUES(:nome,:numero,:cgm,:status,:turma_id)",params);
    }

    public Optional<AlunoResponse> findByCgm(String cgm) {
        return namedParameterJdbcTemplate.query("SELECT a.id,a.nome,a.numero,a.cgm,a.status,a.turma_id,t.ensino,t.ano,t.letra FROM alunos_2026 a JOIN turmas t ON t.id=a.turma_id WHERE a.cgm=:cgm",
            new MapSqlParameterSource().addValue("cgm",cgm),mapper).stream().findFirst();
    }
}

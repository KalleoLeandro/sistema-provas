package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Turma;
import br.com.provas.model.request.AlunoRequest;
import br.com.provas.model.request.ImportarAlunosRequest;
import br.com.provas.model.response.*;
import br.com.provas.repository.AlunoRepository;
import br.com.provas.support.CsvParser;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Valida e importa alunos por CSV, sem permitir cadastro parcial ou atualização de CGMs existentes. */
@Service
public class AlunoImportacaoService {
    private final AlunoRepository repository;
    private final Validator validator;
    public AlunoImportacaoService(AlunoRepository repository,Validator validator){this.repository=repository;this.validator=validator;}
    private record Candidato(int linha,AlunoRequest dados,Turma turma) {}
    private record Analise(ImportacaoAlunosResponse resposta,List<AlunoRequest> alunos) {}

    /** @param request conteúdo do arquivo @return prévia com alunos válidos e erros por linha, sem gravar */
    @Transactional(readOnly=true)
    public ImportacaoAlunosResponse validate(ImportarAlunosRequest request) {
        /* A prévia usa as mesmas validações do cadastro individual e não altera o banco. */
        return analyze(request).resposta();
    }

    /** @param request CSV confirmado pelo administrador @return resultado integral ou erros que impediram o lote */
    @Transactional
    public ImportacaoAlunosResponse importCsv(ImportarAlunosRequest request) {
        /* Revalida no momento de gravar: a prévia pode ter ficado desatualizada por outro cadastro. */
        var analise=analyze(request);
        if(!analise.resposta().erros().isEmpty())return analise.resposta();
        /* Bloqueia as turmas em ordem estável e grava todo o lote na mesma transação.
         * Uma duplicação concorrente desfaz todas as inserções, sem importação parcial. */
        for(Long id:analise.alunos().stream().map(AlunoRequest::turmaId).distinct().sorted().toList()) {
            if(!repository.turmaExists(id))throw new DefaultErrorException("Uma turma foi removida. Valide o CSV novamente.");
        }
        try { repository.saveAll(analise.alunos()); }
        catch(DuplicateKeyException exception){throw new DefaultErrorException("Um CGM foi cadastrado durante a importação. Nenhum aluno foi importado. Valide o arquivo novamente.",HttpStatus.CONFLICT);}
        var r=analise.resposta();return new ImportacaoAlunosResponse(r.total(),r.validos(),r.validos(),r.linhas(),List.of());
    }

    /** @param request CSV recebido @return dados normalizados e relatório completo de validação */
    private Analise analyze(ImportarAlunosRequest request) {
        /* Limites mantêm a leitura em memória controlada, sem salvar o arquivo no servidor. */
        String csv=request.csv();
        if(csv.getBytes(StandardCharsets.UTF_8).length>524288)throw new DefaultErrorException("O CSV deve ter até 512 KB.");
        if(csv.indexOf('\uFFFD')>=0)throw new DefaultErrorException("O CSV contém caracteres inválidos. Exporte o arquivo em UTF-8.");
        var rows=CsvParser.parse(csv);
        if(rows.isEmpty())throw new DefaultErrorException("O CSV está vazio.");
        var errors=new ArrayList<ErroImportacaoAlunoResponse>();
        var header=new LinkedHashMap<String,Integer>();
        var allowed=Set.of("nome","numero","cgm","status","ensino","ano","letra");
        var first=rows.getFirst();
        for(int i=0;i<first.colunas().size();i++) {
            String name=normalize(first.colunas().get(i));
            if(!allowed.contains(name))error(errors,first.numero(),"cabecalho","Coluna desconhecida: "+first.colunas().get(i));
            else if(header.putIfAbsent(name,i)!=null)error(errors,first.numero(),"cabecalho","Coluna repetida: "+name);
        }
        for(String required:List.of("nome","cgm","status","ensino","ano","letra"))if(!header.containsKey(required))error(errors,first.numero(),"cabecalho","Coluna obrigatória ausente: "+required);
        if(rows.size()==1)error(errors,first.numero(),"arquivo","O CSV não contém alunos para importar.");
        if(!errors.isEmpty())return new Analise(new ImportacaoAlunosResponse(rows.size()-1,0,0,List.of(),errors),List.of());
        /* Resolve as turmas por ensino, ano e letra: o arquivo não precisa conhecer IDs internos. */
        Map<String,Turma> turmas=new HashMap<>();
        repository.findImportTurmas().forEach(t->turmas.put(t.ensino()+":"+t.ano()+":"+t.letra(),t));
        var candidates=new ArrayList<Candidato>();var cgms=new LinkedHashMap<String,List<Integer>>();
        for(var row:rows.subList(1,rows.size())) {
            int line=row.numero();
            if(row.colunas().size()!=first.colunas().size()){error(errors,line,"colunas","Quantidade de colunas diferente do cabeçalho.");continue;}
            String nome=value(row.colunas(),header,"nome").replaceAll("\\s+"," ");
            String cgm=value(row.colunas(),header,"cgm");
            String status=normalize(value(row.colunas(),header,"status")).toUpperCase(Locale.ROOT);
            if(status.equals("MATRICULADO"))status="M";if(status.equals("TRANSFERIDO"))status="T";if(status.equals("REMANEJADO"))status="R";if(status.equals("DESISTENTE"))status="D";
            String ensino=normalize(value(row.colunas(),header,"ensino")).toUpperCase(Locale.ROOT);
            if(ensino.equals("FUNDAMENTAL"))ensino="F";if(ensino.equals("MEDIO"))ensino="M";
            String letra=value(row.colunas(),header,"letra").toUpperCase(Locale.ROOT);
            Integer ano=null;BigDecimal numero=null;
            try { ano=Integer.valueOf(value(row.colunas(),header,"ano")); }catch(NumberFormatException ignored){}
            if(!Set.of("F","M").contains(ensino))error(errors,line,"ensino","Informe F (Fundamental) ou M (Médio).");
            if(ano==null || ano<1 || ano>9)error(errors,line,"ano","Informe um ano de 1 a 9.");
            if(!letra.matches("[A-Z]"))error(errors,line,"letra","Informe uma letra de A a Z.");
            String rawNumero=value(row.colunas(),header,"numero");
            try { if(!rawNumero.isBlank())numero=new BigDecimal(rawNumero); }
            catch(NumberFormatException exception){error(errors,line,"numero","Informe um inteiro positivo ou deixe vazio.");}
            Turma turma=turmas.get(ensino+":"+ano+":"+letra);
            if(turma==null && Set.of("F","M").contains(ensino) && ano!=null && ano>=1 && ano<=9 && letra.matches("[A-Z]"))error(errors,line,"turma","Não existe turma cadastrada para o ensino, ano e letra informados.");
            var aluno=new AlunoRequest(nome,numero,cgm,status,turma==null?null:turma.id());
            /* Bean Validation mantém as regras de nome, CGM, número e status iguais ao cadastro individual. */
            for(var violation:validator.validate(aluno)) {
                String field=violation.getPropertyPath().toString();
                if(!field.equals("turmaId"))error(errors,line,field,violation.getMessage());
            }
            if(cgm.matches("[0-9]{1,30}"))cgms.computeIfAbsent(cgm,k->new ArrayList<>()).add(line);
            if(turma!=null)candidates.add(new Candidato(line,aluno,turma));
        }
        /* CGM duplicado no arquivo ou já cadastrado impede todo o lote, sem substituir alunos. */
        var existing=repository.findExistingCgms(new ArrayList<>(cgms.keySet()));
        cgms.forEach((cgm,lines)->{
            if(lines.size()>1)lines.forEach(line->error(errors,line,"cgm","CGM repetido no arquivo."));
            if(existing.contains(cgm))lines.forEach(line->error(errors,line,"cgm","CGM já cadastrado."));
        });
        var invalidLines=new HashSet<Integer>();errors.forEach(e->invalidLines.add(e.linha()));
        var valid=candidates.stream().filter(c->!invalidLines.contains(c.linha())).toList();
        var preview=valid.stream().map(c->{var a=c.dados();var t=c.turma();return new LinhaImportacaoAlunoResponse(c.linha(),a.nome(),a.numero()==null?null:a.numero().intValueExact(),a.cgm(),a.status(),t.id(),t.ensino(),t.ano(),t.letra());}).toList();
        errors.sort(Comparator.comparingInt(ErroImportacaoAlunoResponse::linha).thenComparing(ErroImportacaoAlunoResponse::campo));
        return new Analise(new ImportacaoAlunosResponse(rows.size()-1,valid.size(),0,preview,errors),valid.stream().map(Candidato::dados).toList());
    }
    /** @param columns valores da linha @param header posições das colunas @param key nome normalizado @return valor sem espaços laterais */
    private String value(List<String> columns,Map<String,Integer> header,String key) {
        /* Número é a única coluna que pode faltar no cabeçalho. */
        return header.containsKey(key)?columns.get(header.get(key)).strip():"";
    }
    /** @param text nome de coluna ou código @return texto comparável, sem acentos */
    private String normalize(String text) {
        /* A normalização aceita cabeçalhos em maiúsculas e o rótulo Número com acento. */
        return Normalizer.normalize(text.strip(),Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
    }
    /** @param errors relatório acumulado @param line linha física @param field coluna @param message orientação de correção */
    private void error(List<ErroImportacaoAlunoResponse> errors,int line,String field,String message) {
        /* O relatório identifica o campo sem repetir o conteúdo pessoal recebido no CSV. */
        errors.add(new ErroImportacaoAlunoResponse(line,field,message));
    }
}

package br.com.provas.model.response;
import java.time.Instant;
public record LiberacaoDetalheResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String token,boolean precisaNovoToken, java.util.List<AssuntoResponse> assuntos, br.com.provas.model.request.ConfiguracaoProvaRequest configuracao,java.util.List<br.com.provas.model.request.ComposicaoProvaRequest> composicao,java.math.BigDecimal valorTotal,Integer quantidadeQuestoes){
 public LiberacaoDetalheResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String token,boolean precisaNovoToken, java.util.List<AssuntoResponse> assuntos){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,duracaoMinutos,token,precisaNovoToken,assuntos,null,java.util.List.of(),java.math.BigDecimal.TEN,10);}

 public LiberacaoDetalheResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String token,boolean precisaNovoToken){this(id,titulo,materiaId,materia,disponivelEm,encerraEm,duracaoMinutos,token,precisaNovoToken,java.util.List.of());}
@Override public String toString(){return "LiberacaoDetalheResponse[token omitido]";}}

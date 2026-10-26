package br.com.provas.model.response;
import java.time.Instant;
public record LiberacaoDetalheResponse(Long id,String titulo,Long materiaId,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String token,boolean precisaNovoToken){@Override public String toString(){return "LiberacaoDetalheResponse[token omitido]";}}

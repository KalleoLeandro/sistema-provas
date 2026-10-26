package br.com.provas.model.response;
import java.math.BigDecimal;
import java.time.Instant;
public record ProvaCorrecaoResumoResponse(Long id,Long liberacaoId,String titulo,String materia,String aluno,String cgm,Long turmaId,String ensino,int ano,String letra,Instant entregueEm,int pendentes,BigDecimal notaParcial) {}

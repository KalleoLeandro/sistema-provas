package br.com.provas.model.response;
import java.time.Instant;
public record RegrasProvaResponse(Long id,String titulo,String materia,int duracaoMinutos,Instant encerraEm,boolean iniciada,boolean submetida) {}

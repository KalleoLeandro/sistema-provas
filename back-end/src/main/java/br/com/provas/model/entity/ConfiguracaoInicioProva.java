package br.com.provas.model.entity;
import java.time.Instant;
public record ConfiguracaoInicioProva(Long id,Long professorId,Long materiaId,Long turmaId,String ensino,Integer ano,String titulo,String materia,Instant disponivelEm,Instant encerraEm,Integer duracaoMinutos,String tokenHash) {}

package br.com.provas.model.entity;
import java.time.Instant;
public record TentativaProva(Long id,Long liberacaoId,Long alunoId,String titulo,String materia,Instant iniciadaEm,Instant encerraEm,Instant submetidaEm) {}

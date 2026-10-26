package br.com.provas.model.response;

/** Totais do aluno sem notas, gabaritos ou conteúdo das demais provas. */
public record ProgressoProvasResponse(long total, long concluidas) {}

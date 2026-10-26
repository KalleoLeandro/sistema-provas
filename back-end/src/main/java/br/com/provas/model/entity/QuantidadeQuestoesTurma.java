package br.com.provas.model.entity;

public record QuantidadeQuestoesTurma(Long materiaId, String materia, Long turmaId, String ensino, Integer ano,
    String letra, int peso05, int peso10, int peso15, int peso20) {}

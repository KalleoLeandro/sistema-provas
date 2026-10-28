package br.com.provas.model.entity;

public record QuantidadeQuestoesTurma(Long materiaId, String materia, Long turmaId, String ensino, Integer ano,
    String letra, int peso05, int peso10, int peso15, int peso20, Long assuntoId, String assunto) {
 public QuantidadeQuestoesTurma(Long materiaId, String materia, Long turmaId, String ensino, Integer ano,
    String letra, int peso05, int peso10, int peso15, int peso20){this(materiaId,materia,turmaId,ensino,ano,letra,peso05,peso10,peso15,peso20,null,null);}
}

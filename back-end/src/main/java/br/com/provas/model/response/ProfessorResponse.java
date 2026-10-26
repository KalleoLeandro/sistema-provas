package br.com.provas.model.response;

import java.util.List;

public record ProfessorResponse(Long id, String nome, String login, boolean ativo, List<TurmaResponse> turmas,
    List<ProfessorMateriaResponse> materias) {}

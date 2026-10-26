package br.com.provas.model.response;

import java.util.List;

public record ProfessorMateriaResponse(Long materiaId, String nome, List<TurmaResponse> turmas) {}

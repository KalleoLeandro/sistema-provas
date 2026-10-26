package br.com.provas.model.entity;

import java.util.List;

public record ProfessorMateria(Long materiaId, String nome, List<Turma> turmas) {}

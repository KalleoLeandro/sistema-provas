package br.com.provas.model.entity;

public record UsuarioAdministrativo(Long id, String nome, String login, String senhaHash, boolean ativo) {}

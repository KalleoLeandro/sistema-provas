package br.com.provas.model.entity;

public record Professor(Long id, String nome, String login, String senhaHash, boolean ativo) {
    @Override
    public String toString() { return "Professor[id=" + id + "]"; }
}

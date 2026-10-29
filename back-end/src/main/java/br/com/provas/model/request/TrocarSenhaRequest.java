package br.com.provas.model.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record TrocarSenhaRequest(@NotBlank @Size(max=128) String senhaAtual, @NotBlank @Size(min=8,max=128) String novaSenha) {}

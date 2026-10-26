package br.com.provas.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.Locale;

@br.com.provas.validation.ValidLogin
public record LoginRequest(
    @NotBlank(message = "O login é obrigatório.")
    @Size(max = 120, message = "O login deve conter até 120 caracteres.")
    String login,
    @Size(max = 128, message = "A senha deve conter até 128 caracteres.")
    String senha,
    @NotBlank(message = "O perfil é obrigatório.")
    @Pattern(regexp = "administrador|professor|aluno", message = "Selecione um perfil válido.")
    String perfil
) {
    public LoginRequest {
        login = login == null ? null : login.trim();
        perfil = perfil == null ? "administrador" : perfil.strip().toLowerCase(Locale.ROOT);
    }
    public LoginRequest(String login, String senha) { this(login, senha, "administrador"); }
    @Override
    public String toString() { return "LoginRequest[credenciais omitidas]"; }
}

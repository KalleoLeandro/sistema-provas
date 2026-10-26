package br.com.provas.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record AtualizarUsuarioAdministrativoRequest(
    @NotBlank(message = "O login é obrigatório.")
    @Size(min = 3, max = 120, message = "O login deve conter de 3 a 120 caracteres.")
    String login,
    @Size(min = 8, max = 128, message = "A senha deve conter de 8 a 128 caracteres.")
    @Pattern(regexp = "(?sU).*\\S.*", message = "A senha não pode conter apenas espaços.")
    String senha
) {
    public AtualizarUsuarioAdministrativoRequest {
        login = login == null ? null : login.trim();
        senha = senha != null && senha.isEmpty() ? null : senha;
    }
    @Override
    public String toString() { return "AtualizarUsuarioAdministrativoRequest[credenciais omitidas]"; }
}

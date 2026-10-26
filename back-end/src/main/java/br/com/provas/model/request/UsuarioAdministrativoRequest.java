package br.com.provas.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioAdministrativoRequest(
    @NotBlank(message = "O login é obrigatório.")
    @Size(min = 3, max = 120, message = "O login deve conter de 3 a 120 caracteres.")
    String login,
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, max = 128, message = "A senha deve conter de 8 a 128 caracteres.")
    String senha
) {
    public UsuarioAdministrativoRequest { login = login == null ? null : login.trim(); }
    @Override
    public String toString() { return "UsuarioAdministrativoRequest[credenciais omitidas]"; }
}

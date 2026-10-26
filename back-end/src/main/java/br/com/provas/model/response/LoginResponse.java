package br.com.provas.model.response;

import java.io.Serializable;
import java.security.Principal;

public record LoginResponse(Long id, String nome, String login, String perfil) implements Serializable, Principal {
    @Override
    public String getName() { return id.toString(); }
}

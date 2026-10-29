package br.com.provas.model.response;

import java.io.Serializable;
import java.security.Principal;

public record LoginResponse(Long id, String nome, String login, String perfil, boolean trocarSenha) implements Serializable, Principal {
    public LoginResponse(Long id,String nome,String login,String perfil) { this(id,nome,login,perfil,false); }
    @Override
    public String getName() { return id.toString(); }
}

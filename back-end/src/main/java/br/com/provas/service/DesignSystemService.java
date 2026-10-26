package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Autoriza o catálogo de componentes exclusivamente para a conta master ativa. */
@Service
public class DesignSystemService {
    private final UsuarioAdministrativoRepository repository;

    public DesignSystemService(UsuarioAdministrativoRepository repository) {
        this.repository = repository;
    }

    /**
     * Confirma a permissão antes de servir qualquer recurso do Storybook.
     * @param usuario identidade obtida da sessão autenticada pelo servidor
     * @throws DefaultErrorException quando não há sessão ou a conta não é master ativa
     */
    public void authorize(LoginResponse usuario) {
        /*
         * A ausência de autenticação é diferenciada da falta de permissão. O perfil
         * precisa ser administrativo para que IDs de outros perfis nunca concedam acesso.
         */
        if (usuario == null) {
            throw new DefaultErrorException("Entre no sistema para acessar o design system.", HttpStatus.UNAUTHORIZED);
        }
        /*
         * A autorização usa o sinalizador master do banco, vinculado ao ID da sessão.
         * Nome, login e dados enviados pelo navegador não concedem essa permissão.
         * Reconsultar a cada acesso também revoga o catálogo ao retirar a permissão.
         */
        if (!"administrador".equals(usuario.perfil()) || !repository.isActiveMaster(usuario.id())) {
            throw new DefaultErrorException("O design system está disponível apenas para o Master User.", HttpStatus.FORBIDDEN);
        }
    }
}

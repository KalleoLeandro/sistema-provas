package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.request.UsuarioAdministrativoRequest;
import br.com.provas.model.response.UsuarioAdministrativoResponse;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import br.com.provas.model.request.StatusRequest;
import br.com.provas.model.request.AtualizarUsuarioAdministrativoRequest;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantém as contas administrativas, garantindo login único e armazenamento de
 * senha apenas como hash. As respostas públicas não contêm o hash da senha.
 * As verificações locais complementam o @Valid dos controllers para proteger
 * chamadas internas e as restrições de unicidade e existência do cadastro.
 */
@Service
public class UsuarioAdministrativoService {
    private final UsuarioAdministrativoRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdministrativoService(UsuarioAdministrativoRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra uma conta administrativa com senha protegida.
     *
     * @param request login e senha da nova conta
     * @return dados públicos da conta criada
     * @throws DefaultErrorException quando os dados são inválidos ou o login está ocupado
     */
    public UsuarioAdministrativoResponse create(UsuarioAdministrativoRequest request) {
        /*
         * Protege os limites do cadastro mesmo em chamadas fora do controller:
         * login de 3 a 120 caracteres e senha preenchida de 8 a 128 caracteres.
         */
        if (request == null || request.login() == null || request.login().trim().length() < 3
            || request.login().trim().length() > 120) {
            throw new DefaultErrorException("O login deve conter de 3 a 120 caracteres.");
        }
        if (request.senha() == null || request.senha().isBlank()
            || request.senha().length() < 8 || request.senha().length() > 128) {
            throw new DefaultErrorException("A senha deve conter de 8 a 128 caracteres.");
        }
        /*
         * Espaços externos não distinguem contas. A consulta prévia identifica
         * um login já utilizado antes de executar a gravação.
         */
        var login = request.login().trim();
        if (repository.findByLogin(login).isPresent()) {
            throw new DefaultErrorException("Este login já está cadastrado.", HttpStatus.CONFLICT);
        }
        /*
         * Somente o hash chega ao repository; o nome inicial deriva do login e
         * o banco aplica o status ativo. A restrição de unicidade continua sendo
         * a garantia final contra duas criações simultâneas do mesmo login.
         */
        try {
            var usuario = repository.save(login, passwordEncoder.encode(request.senha()));
            return new UsuarioAdministrativoResponse(usuario.id(), usuario.nome(), usuario.login(), usuario.ativo());
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Este login já está cadastrado.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Lista as contas visíveis ao administrador autenticado.
     *
     * @param usuarioId identificador do usuário da sessão
     * @return dados públicos das contas permitidas na listagem
     */
    public List<UsuarioAdministrativoResponse> findAll(Long usuarioId) {
        /*
         * A conta master só integra a lista quando pertence ao usuário da sessão.
         * O repository aplica essa regra pelo ID, e a resposta não expõe hashes.
         */
        return repository.findAllVisibleTo(usuarioId).stream().map(usuario -> new UsuarioAdministrativoResponse(
            usuario.id(), usuario.nome(), usuario.login(), usuario.ativo())).toList();
    }

    /**
     * Consulta os dados públicos de uma conta administrativa.
     *
     * @param id identificador da conta
     * @return dados usados na consulta ou edição
     * @throws DefaultErrorException quando a conta não existe
     */
    public UsuarioAdministrativoResponse findById(Long id) {
        /*
         * Uma conta inexistente não pode originar um formulário de edição.
         * Somente os dados públicos são retornados, preservando o hash da senha.
         */
        var usuario = repository.findById(id).orElseThrow(() ->
            new DefaultErrorException("Administrador não encontrado.", HttpStatus.NOT_FOUND));
        return new UsuarioAdministrativoResponse(usuario.id(), usuario.nome(), usuario.login(), usuario.ativo());
    }

    /**
     * Atualiza as credenciais de uma conta administrativa.
     *
     * @param id identificador da conta
     * @param request login e eventual nova senha
     * @return dados públicos atualizados
     * @throws DefaultErrorException quando os dados são inválidos, a conta não existe ou o login está ocupado
     */
    @Transactional
    public UsuarioAdministrativoResponse update(Long id, AtualizarUsuarioAdministrativoRequest request) {
        /*
         * Revalida o login para proteger também chamadas internas. Esta operação
         * modifica credenciais; nome e status pertencem a outros dados do cadastro.
         */
        if (request == null || request.login() == null || request.login().trim().length() < 3
            || request.login().trim().length() > 120) {
            throw new DefaultErrorException("O login deve conter de 3 a 120 caracteres.");
        }
        /*
         * Senha ausente ou vazia significa manter o hash atual. Quando uma nova
         * senha é fornecida, ela precisa cumprir integralmente a regra do cadastro.
         */
        boolean alterarSenha = request.senha() != null && !request.senha().isEmpty();
        if (alterarSenha && (request.senha().isBlank() || request.senha().length() < 8 || request.senha().length() > 128)) {
            throw new DefaultErrorException("A senha deve conter de 8 a 128 caracteres.");
        }
        /*
         * A conta precisa existir e pode manter seu próprio login. O conflito
         * ocorre somente quando o novo login já pertence a outra identidade.
         */
        var usuario = repository.findById(id).orElseThrow(() ->
            new DefaultErrorException("Administrador não encontrado.", HttpStatus.NOT_FOUND));
        var login = request.login().trim();
        if (repository.findByLogin(login).filter(conta -> !conta.id().equals(id)).isPresent()) {
            throw new DefaultErrorException("Este login já está cadastrado.", HttpStatus.CONFLICT);
        }
        /*
         * A gravação preserva nome e status e escolhe entre um novo hash e o atual.
         * A transação e a restrição de unicidade protegem a alteração concorrente.
         * Ao mudar o login, sessões com o login anterior falham na revalidação.
         */
        try {
            repository.update(id, login, alterarSenha ? passwordEncoder.encode(request.senha()) : usuario.senhaHash());
            return new UsuarioAdministrativoResponse(id, usuario.nome(), login, usuario.ativo());
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Este login já está cadastrado.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Ativa ou desativa uma conta administrativa.
     *
     * @param id identificador da conta
     * @param request status solicitado explicitamente
     * @return dados públicos com o novo status
     * @throws DefaultErrorException quando o status é ausente ou a conta não existe
     */
    @Transactional
    public UsuarioAdministrativoResponse updateStatus(Long id, StatusRequest request) {
        /*
         * O status precisa ser explícito: ausência de informação não pode ser
         * interpretada como false e desativar uma conta acidentalmente.
         */
        if (request == null || request.ativo() == null) {
            throw new DefaultErrorException("Informe o status ativo ou inativo.");
        }
        /*
         * Altera somente o status de uma conta existente. A inativação bloqueia
         * novos logins, e o filtro de sessão passa a recusar acessos em andamento
         * assim que reconsulta a situação da conta.
         */
        var usuario = repository.findById(id).orElseThrow(() ->
            new DefaultErrorException("Administrador não encontrado.", HttpStatus.NOT_FOUND));
        repository.updateStatus(id, request.ativo());
        return new UsuarioAdministrativoResponse(usuario.id(), usuario.nome(), usuario.login(), request.ativo());
    }
}

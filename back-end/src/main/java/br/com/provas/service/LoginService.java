package br.com.provas.service;

import br.com.provas.model.request.LoginRequest;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import br.com.provas.repository.ProfessorRepository;
import br.com.provas.repository.AlunoRepository;
import java.util.UUID;
import br.com.provas.exceptions.DefaultErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Autentica contas administrativas e de professores e consulta se continuam aptas a acessar o sistema.
 * A criação da sessão HTTP pertence ao fluxo do controller; esta service decide
 * apenas se as credenciais e a situação da conta permitem o acesso.
 */
@Service
public class LoginService {
    private final UsuarioAdministrativoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ProfessorRepository professorRepository;
    private final String dummyHash;
    private final AlunoRepository alunoRepository;
    private record ContaAcesso(Long id, String nome, String login, String senhaHash, boolean ativo) {
        @Override public String toString() { return "ContaAcesso[credenciais omitidas]"; }
    }

    public LoginService(UsuarioAdministrativoRepository repository, ProfessorRepository professorRepository, PasswordEncoder passwordEncoder, AlunoRepository alunoRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * Autentica uma conta ativa exclusivamente no perfil selecionado.
     *
     * @param request credenciais informadas para autenticação
     * @return identidade pública da conta autenticada
     * @throws DefaultErrorException quando as credenciais não autorizam o acesso
     */
    public LoginResponse login(LoginRequest request) {
        /* Alunos usam apenas CGM; o perfil nunca tenta autenticar uma conta de professor ou administrador. */
        if(request!=null && "aluno".equals(request.perfil())) {
            if(request.login()==null || !request.login().matches("[0-9]{1,30}")) throw new DefaultErrorException("Credenciais inválidas.",HttpStatus.UNAUTHORIZED);
            var aluno=alunoRepository.findByCgm(request.login()).filter(a->"M".equals(a.status()))
                .orElseThrow(()->new DefaultErrorException("Credenciais inválidas.",HttpStatus.UNAUTHORIZED));
            return new LoginResponse(aluno.id(),aluno.nome(),aluno.cgm(),"aluno");
        }
        /*
         * Também protege chamadas internas que não passam pela validação do controller.
         * Credenciais incompletas não podem iniciar uma tentativa de autenticação.
         */
        if (request == null || request.login() == null || request.senha() == null
            || request.login().isBlank() || request.login().length() > 120
            || request.senha().isEmpty() || request.senha().length() > 1024
            || !("administrador".equals(request.perfil()) || "professor".equals(request.perfil()))) {
            throw new DefaultErrorException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }
        /*
         * Compara a senha mesmo quando o login não existe, usando o hash auxiliar.
         * Isso reduz diferenças de tempo que poderiam revelar contas cadastradas.
         */
        /* O perfil escolhe a origem da conta; nunca há fallback que transforme um professor em administrador. */
        var usuario = "professor".equals(request.perfil())
            ? professorRepository.findByLogin(request.login().trim()).map(p -> new ContaAcesso(p.id(), p.nome(), p.login(), p.senhaHash(), p.ativo()))
            : repository.findByLogin(request.login().trim()).map(p -> new ContaAcesso(p.id(), p.nome(), p.login(), p.senhaHash(), p.ativo()));
        boolean senhaValida = passwordEncoder.matches(request.senha(),
            usuario.map(value -> value.senhaHash()).orElse(dummyHash));
        /*
         * Conta inexistente, senha incorreta e conta inativa recebem a mesma recusa.
         * Somente a combinação de identidade, senha e status válidos libera o acesso.
         */
        if (usuario.isEmpty() || !senhaValida || !usuario.get().ativo()) {
            throw new DefaultErrorException("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }
        var conta = usuario.get();
        return current(new LoginResponse(conta.id(), conta.nome(), conta.login(), request.perfil()));
    }

    /**
     * Verifica se a identidade da sessão ainda corresponde a uma conta ativa.
     *
     * @param usuario identidade obtida da sessão
     * @return se a conta ainda está apta a acessar o sistema
     */
    public boolean isActive(LoginResponse usuario) {
        /*
         * Reconsulta o cadastro para que desativação ou mudança de login também
         * bloqueiem sessões já abertas na próxima passagem pelo filtro de sessão.
         */
        /* Transferência e mudança de CGM bloqueiam também sessões de aluno que já estavam abertas. */
        if("aluno".equals(usuario.perfil())) return alunoRepository.findById(usuario.id())
            .filter(a->"M".equals(a.status()) && a.cgm().equals(usuario.login())).isPresent();
        if ("professor".equals(usuario.perfil())) {
            return professorRepository.findByLogin(usuario.login())
                .filter(conta -> conta.id().equals(usuario.id()) && conta.ativo()).isPresent();
        }
        return "administrador".equals(usuario.perfil()) && repository.findByLogin(usuario.login())
            .filter(conta -> conta.id().equals(usuario.id()) && conta.ativo()).isPresent();
    }

 public LoginResponse current(LoginResponse u) {
  boolean pending="professor".equals(u.perfil()) ? professorRepository.requiresPasswordChange(u.id()) : "administrador".equals(u.perfil()) && repository.requiresPasswordChange(u.id());
  return new LoginResponse(u.id(),u.nome(),u.login(),u.perfil(),pending);
 }
 public void changePassword(LoginResponse u,String atual,String nova) {
  if ("aluno".equals(u.perfil())) throw new DefaultErrorException("Perfil inválido.",HttpStatus.FORBIDDEN);
  String hash="professor".equals(u.perfil()) ? professorRepository.findById(u.id()).orElseThrow().senhaHash() : repository.findById(u.id()).orElseThrow().senhaHash();
  if (!passwordEncoder.matches(atual,hash)) throw new DefaultErrorException("Senha atual incorreta.");
  if (passwordEncoder.matches(nova,hash)) throw new DefaultErrorException("A nova senha deve ser diferente da senha atual.");
  String encoded=passwordEncoder.encode(nova);
  int count="professor".equals(u.perfil()) ? professorRepository.changePassword(u.id(),hash,encoded) : repository.changePassword(u.id(),hash,encoded);
  if(count!=1) throw new DefaultErrorException("A conta foi alterada. Entre novamente.",HttpStatus.CONFLICT);
 }
}

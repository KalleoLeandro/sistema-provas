package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.SessionResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Define a duração e a possibilidade de renovação das sessões autenticadas.
 * Todos os perfis recebem oito horas, mas somente professores e administradores
 * podem renovar o prazo. O horário absoluto de expiração integra a sessão
 * persistida, para que uma reinicialização do back-end não reinicie a contagem.
 */
@Service
public class SessionService {
    private static final String EXPIRES_AT = SessionService.class.getName() + ".expiresAt";
    private final Clock clock;

    public SessionService(Clock clock) { this.clock = clock; }

    /**
     * Define a validade inicial de uma sessão autenticada.
     *
     * @param session sessão que receberá o prazo de acesso
     * @param perfil perfil autenticado
     * @return vencimento da sessão e permissão de refresh
     * @throws DefaultErrorException quando o perfil não é reconhecido
     */
    public SessionResponse start(HttpSession session, String perfil) {
        /*
         * Todos os perfis reconhecidos recebem oito horas de acesso. A diferença
         * entre eles está na permissão de renovação, não na duração inicial.
         */
        Duration duration = switch (perfil) {
            case "aluno" -> Duration.ofHours(8);
            case "professor", "administrador" -> Duration.ofHours(8);
            default -> throw new DefaultErrorException("Perfil inválido.", HttpStatus.FORBIDDEN);
        };
        /*
         * Grava o vencimento absoluto na sessão persistida. O timeout de inatividade
         * acompanha a duração, mas navegar pelo sistema ou reiniciar o back-end
         * não altera esse horário de vencimento.
         */
        Instant expiresAt = clock.instant().plus(duration);
        session.setMaxInactiveInterval(Math.toIntExact(duration.toSeconds()));
        session.setAttribute(EXPIRES_AT, expiresAt);
        /*
         * O aluno não pode renovar o prazo; os demais perfis reconhecidos podem
         * solicitar refresh enquanto a sessão ainda estiver válida.
         */
        return new SessionResponse(expiresAt, !"aluno".equals(perfil));
    }

    /**
     * Verifica a validade temporal da sessão.
     *
     * @param session sessão atual, que pode estar ausente
     * @return se o acesso está vencido ou não possui prazo válido
     */
    public boolean isExpired(HttpSession session) {
        /*
         * Sessões sem o prazo de uma autenticação válida não liberam acesso.
         * No instante exato do vencimento a sessão já é considerada expirada;
         * consultar essa condição nunca prorroga o prazo.
         */
        return session == null || !(session.getAttribute(EXPIRES_AT) instanceof Instant expiresAt)
            || !clock.instant().isBefore(expiresAt);
    }

    /**
     * Renova uma sessão válida de professor ou administrador.
     *
     * @param session sessão atual
     * @param perfil perfil que solicita renovação
     * @return novo vencimento contado a partir da renovação
     * @throws DefaultErrorException quando a sessão expirou ou o perfil não permite refresh
     */
    public SessionResponse refresh(HttpSession session, String perfil) {
        /*
         * Uma sessão vencida exige novo login e deve ser invalidada. Refresh não
         * pode recuperar uma autenticação que já ultrapassou o prazo de acesso.
         */
        if (isExpired(session)) {
            if (session != null) session.invalidate();
            throw new DefaultErrorException("Sessão expirada. Entre novamente.", HttpStatus.UNAUTHORIZED);
        }
        /*
         * Somente professor e administrador podem iniciar outras oito horas.
         * Para aluno, a recusa não modifica o prazo da sessão ainda válida.
         */
        if (!"professor".equals(perfil) && !"administrador".equals(perfil)) {
            throw new DefaultErrorException("Refresh não disponível para este perfil.", HttpStatus.FORBIDDEN);
        }
        return start(session, perfil);
    }
}

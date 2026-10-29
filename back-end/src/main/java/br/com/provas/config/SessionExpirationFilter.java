package br.com.provas.config;

import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.SessionService;
import br.com.provas.service.LoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionExpirationFilter extends OncePerRequestFilter {
    private final SessionService service;
    private final LoginService loginService;

    public SessionExpirationFilter(SessionService service, LoginService loginService) {
        this.service = service;
        this.loginService = loginService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginResponse usuario
            && (service.isExpired(request.getSession(false)) || !loginService.isActive(usuario))) {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
            SecurityContextHolder.clearContext();
        }
        var current = SecurityContextHolder.getContext().getAuthentication();
        if(current != null && current.getPrincipal() instanceof LoginResponse usuario
            && loginService.current(usuario).trocarSenha()
            && !java.util.Set.of("/login/me","/login/csrf","/login/logout","/login/trocar-senha","/login/validar-login").contains(request.getServletPath())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"mensagem\":\"Troque sua senha para continuar.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}

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
        chain.doFilter(request, response);
    }
}

package br.com.provas.config;

import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.LoginService;
import br.com.provas.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordChangeFilterTest {
    @Test void pendingPasswordBlocksApiButAllowsRecoveryEndpoints() throws Exception {
        var sessions = mock(SessionService.class);
        var login = mock(LoginService.class);
        var user = new LoginResponse(1L,"Admin","admin","administrador",false);
        when(login.isActive(user)).thenReturn(true);
        // The database state takes precedence over a session opened before the reset.
        when(login.current(user)).thenReturn(new LoginResponse(1L,"Admin","admin","administrador",true));
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(user,null,List.of()));
        try {
            for (String path : List.of("/dashboard","/login/me","/login/csrf","/login/trocar-senha","/login/logout")) {
                var request = new MockHttpServletRequest();
                request.setServletPath(path);
                var response = new MockHttpServletResponse();
                var chain = mock(FilterChain.class);
                new SessionExpirationFilter(sessions,login).doFilter(request,response,chain);
                if (path.equals("/dashboard")) {
                    assertEquals(403,response.getStatus());
                    verifyNoInteractions(chain);
                } else {
                    verify(chain).doFilter(request,response);
                }
            }
        } finally { SecurityContextHolder.clearContext(); }
    }
}

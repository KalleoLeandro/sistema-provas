package br.com.provas.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import br.com.provas.model.request.LoginRequest;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.LoginService;
import br.com.provas.service.SessionService;
import br.com.provas.model.response.SessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {
	private final LoginService service;
	private final SecurityContextRepository contextRepository;
	private final SessionAuthenticationStrategy sessionStrategy;
	private final SessionService sessionService;

	public LoginController(LoginService service, SecurityContextRepository contextRepository,
			SessionAuthenticationStrategy sessionStrategy, SessionService sessionService) {
		this.service = service;
		this.contextRepository = contextRepository;
		this.sessionStrategy = sessionStrategy;
		this.sessionService = sessionService;
	}

	@GetMapping("/csrf")
	public Map<String, String> csrf(CsrfToken token) {
		return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
	}

	@PostMapping("/validar-login")
	public LoginResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		var usuario = service.login(body);
		var authentication = UsernamePasswordAuthenticationToken.authenticated(usuario, null,
				List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().toUpperCase(Locale.ROOT))));
		sessionStrategy.onAuthentication(authentication, request, response);
		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		contextRepository.saveContext(context, request, response);
		sessionService.start(request.getSession(), usuario.perfil());
		return usuario;
	}

	@GetMapping("/me")
	public LoginResponse me(@AuthenticationPrincipal LoginResponse usuario) {
		return usuario;
	}

	@PostMapping("/refresh")
	public SessionResponse refresh(@AuthenticationPrincipal LoginResponse usuario, HttpServletRequest request) {
		return sessionService.refresh(request.getSession(false), usuario.perfil());
	}

}

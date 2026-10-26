package br.com.provas.config;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.DispatcherType;
import java.util.List;
import br.com.provas.service.SessionService;
import br.com.provas.service.LoginService;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(CsrfTokenRepository csrfRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
            new ChangeSessionIdAuthenticationStrategy(), new CsrfAuthenticationStrategy(csrfRepository)));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            SecurityContextRepository contextRepository, CsrfTokenRepository csrfRepository,
            SessionService sessionService, LoginService loginService, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
            .authorizeHttpRequests(access -> access
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/login/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/login/validar-login").permitAll()
                .requestMatchers(HttpMethod.POST, "/login/refresh").hasAnyRole("PROFESSOR", "ADMINISTRADOR")
                .requestMatchers("/dashboard", "/dashboard/**").hasAnyRole("ADMINISTRADOR", "PROFESSOR")
                .requestMatchers("/relatorios", "/relatorios/**").hasAnyRole("ADMINISTRADOR", "PROFESSOR")
                .requestMatchers("/administradores", "/administradores/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/materias", "/materias/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/turmas", "/turmas/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/professores", "/professores/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/alunos", "/alunos/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/aluno", "/aluno/**").hasRole("ALUNO")
                .requestMatchers("/professor", "/professor/**").hasRole("PROFESSOR")
                .requestMatchers("/questoes", "/questoes/**").hasRole("PROFESSOR")
                .requestMatchers("/design-system", "/design-system/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated())
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .securityContext(context -> context.securityContextRepository(contextRepository))
            .addFilterAfter(new SessionExpirationFilter(sessionService, loginService), SecurityContextHolderFilter.class)
            .requestCache(cache -> cache.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .rememberMe(remember -> remember.disable())
            .exceptionHandling(errors -> errors.authenticationEntryPoint(
                (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
            .logout(logout -> logout.logoutUrl("/login/logout")
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                .logoutSuccessHandler(
                (request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .build();
    }
}

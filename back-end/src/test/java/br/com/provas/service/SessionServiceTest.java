package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC);
    private final SessionService service = new SessionService(clock);

    @Test
    void alunoExpiresAfterEightHoursWithoutRefresh() {
        var session = new MockHttpSession();
        var response = service.start(session, "aluno");
        assertEquals(28800, session.getMaxInactiveInterval());
        assertEquals(clock.instant().plusSeconds(28800), response.expiresAt());
        assertFalse(response.refreshAllowed());
        var exception = assertThrows(DefaultErrorException.class, () -> service.refresh(session, "aluno"));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void professorAndAdministradorHaveEightHours() {
        for (var perfil : new String[] { "professor", "administrador" }) {
            var session = new MockHttpSession();
            var response = service.start(session, perfil);
            assertEquals(28800, session.getMaxInactiveInterval());
            assertEquals(clock.instant().plusSeconds(28800), response.expiresAt());
            assertTrue(response.refreshAllowed());
        }
    }

    @Test
    void refreshGrantsEightHoursFromCurrentTime() {
        for (var perfil : new String[] { "professor", "administrador" }) {
            var session = new MockHttpSession();
            service.start(session, perfil);
            var laterClock = Clock.offset(clock, Duration.ofHours(7));
            var response = new SessionService(laterClock).refresh(session, perfil);
            assertEquals(laterClock.instant().plus(Duration.ofHours(8)), response.expiresAt());
        }
    }

    @Test
    void normalActivityDoesNotExtendDeadline() {
        var session = new MockHttpSession();
        service.start(session, "aluno");
        var before = new SessionService(Clock.offset(clock, Duration.ofSeconds(28799)));
        assertFalse(before.isExpired(session));
        var after = new SessionService(Clock.offset(clock, Duration.ofHours(8)));
        assertTrue(after.isExpired(session));
    }

    @Test
    void refreshCannotRestoreExpiredSession() {
        var session = new MockHttpSession();
        service.start(session, "administrador");
        var after = new SessionService(Clock.offset(clock, Duration.ofHours(8)));
        var exception = assertThrows(DefaultErrorException.class, () -> after.refresh(session, "administrador"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertTrue(session.isInvalid());
    }

    @Test
    void missingSessionRequiresNewLogin() {
        var exception = assertThrows(DefaultErrorException.class, () -> service.refresh(null, "administrador"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}

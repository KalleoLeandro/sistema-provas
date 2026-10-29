package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Professor;
import br.com.provas.model.entity.UsuarioAdministrativo;
import br.com.provas.model.request.LoginRequest;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.ProfessorRepository;
import br.com.provas.repository.UsuarioAdministrativoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginServiceTest {
    @Test void newOrResetAccountRequiresPasswordChange() {
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L,"Professor","prof","hash",true)));
        when(encoder.matches("temporaria", "hash")).thenReturn(true);
        when(professores.requiresPasswordChange(1L)).thenReturn(true);
        assertTrue(service.login(new LoginRequest("prof","temporaria","professor")).trocarSenha());
        when(professores.requiresPasswordChange(1L)).thenReturn(false);
        assertFalse(service.current(new LoginResponse(1L,"Professor","prof","professor",true)).trocarSenha());
    }

    @Test void changePasswordChecksCurrentPasswordAndRejectsReuse() {
        var usuario = new LoginResponse(1L,"Admin","admin","administrador",true);
        when(administradores.findById(1L)).thenReturn(Optional.of(new UsuarioAdministrativo(1L,"Admin","admin","hash",true)));
        assertThrows(DefaultErrorException.class, () -> service.changePassword(usuario,"errada","nova-senha"));
        when(encoder.matches("temporaria","hash")).thenReturn(true);
        assertThrows(DefaultErrorException.class, () -> service.changePassword(usuario,"temporaria","temporaria"));
        verify(administradores,never()).changePassword(anyLong(),anyString(),anyString());
        when(encoder.encode("nova-senha")).thenReturn("novo-hash");
        when(administradores.changePassword(1L,"hash","novo-hash")).thenReturn(1);
        service.changePassword(usuario,"temporaria","nova-senha");
        verify(administradores).changePassword(1L,"hash","novo-hash");
    }
    private final UsuarioAdministrativoRepository administradores = mock(UsuarioAdministrativoRepository.class);
    private final ProfessorRepository professores = mock(ProfessorRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private LoginService service;
    private final br.com.provas.repository.AlunoRepository alunos=mock(br.com.provas.repository.AlunoRepository.class);

    @BeforeEach void setup() {
        when(encoder.encode(anyString())).thenReturn("dummy");
        service = new LoginService(administradores, professores, encoder, alunos);
    }

    @Test void professorReceivesOnlyProfessorIdentity() {
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L, "Professor", "prof", "hash-prof", true)));
        when(encoder.matches("senha-teste", "hash-prof")).thenReturn(true);
        var result = service.login(new LoginRequest("prof", "senha-teste", "professor"));
        assertEquals("professor", result.perfil()); assertEquals("Professor", result.nome());
        verifyNoInteractions(administradores);
    }

    @Test void administradorKeepsAdministrativeIdentity() {
        when(administradores.findByLogin("admin")).thenReturn(Optional.of(new UsuarioAdministrativo(1L, "Admin", "admin", "hash-admin", true)));
        when(encoder.matches("senha-teste", "hash-admin")).thenReturn(true);
        assertEquals("administrador", service.login(new LoginRequest("admin", "senha-teste", "administrador")).perfil());
        verifyNoInteractions(professores);
    }

    @Test void wrongProfileDoesNotFallBackToAnotherRepository() {
        when(professores.findByLogin("admin")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(DefaultErrorException.class,
            () -> service.login(new LoginRequest("admin", "senha-teste", "professor"))).getStatus());
        verify(encoder).matches("senha-teste", "dummy"); verifyNoInteractions(administradores);
    }

    @Test void inactiveProfessorCannotLoginEvenWithCorrectPassword() {
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L, "Professor", "prof", "hash-prof", false)));
        when(encoder.matches("senha-teste", "hash-prof")).thenReturn(true);
        assertThrows(DefaultErrorException.class, () -> service.login(new LoginRequest("prof", "senha-teste", "professor")));
    }

    @Test void incorrectPasswordIsRejected() {
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L, "Professor", "prof", "hash-prof", true)));
        assertThrows(DefaultErrorException.class, () -> service.login(new LoginRequest("prof", "senha-errada", "professor")));
    }

    @Test void sessionValidationUsesProfessorRepositoryEvenWhenIdsOverlap() {
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L, "Professor", "prof", "hash-prof", true)));
        var principal = new LoginResponse(1L, "Professor", "prof", "professor");
        assertTrue(service.isActive(principal));
        when(professores.findByLogin("prof")).thenReturn(Optional.of(new Professor(1L, "Professor", "prof", "hash-prof", false)));
        assertFalse(service.isActive(principal)); verifyNoInteractions(administradores);
    }
    @Test void studentUsesOnlyCgmAndCannotAccessWhenTransferred(){
        var aluno=new br.com.provas.model.response.AlunoResponse(9L,"Ana",1,"00123","M",5L,"F",1,"A");
        when(alunos.findByCgm("00123")).thenReturn(Optional.of(aluno));
        var response=service.login(new LoginRequest("00123",null,"aluno"));
        assertEquals("aluno",response.perfil());assertEquals(9L,response.id());
        verifyNoInteractions(administradores,professores);
        when(alunos.findByCgm("00123")).thenReturn(Optional.of(new br.com.provas.model.response.AlunoResponse(9L,"Ana",1,"00123","T",5L,"F",1,"A")));
        assertThrows(DefaultErrorException.class,()->service.login(new LoginRequest("00123",null,"aluno")));
        assertThrows(DefaultErrorException.class,()->service.login(new LoginRequest("ABC",null,"aluno")));
    }
    @Test void studentSessionChecksIdentityAndEnrollmentAgain(){
        var response=new LoginResponse(9L,"Ana","00123","aluno");
        when(alunos.findById(9L)).thenReturn(Optional.of(new br.com.provas.model.response.AlunoResponse(9L,"Ana",1,"00123","M",5L,"F",1,"A")));
        assertTrue(service.isActive(response));
        when(alunos.findById(9L)).thenReturn(Optional.empty());assertFalse(service.isActive(response));
    }
}

package br.com.provas.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import br.com.provas.model.request.UsuarioAdministrativoRequest;
import br.com.provas.model.response.UsuarioAdministrativoResponse;
import br.com.provas.service.UsuarioAdministrativoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import br.com.provas.model.request.StatusRequest;
import java.util.List;
import br.com.provas.model.request.AtualizarUsuarioAdministrativoRequest;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/administradores")
public class UsuarioAdministrativoController {
    private final UsuarioAdministrativoService service;

    public UsuarioAdministrativoController(UsuarioAdministrativoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdministrativoResponse create(@Valid @RequestBody UsuarioAdministrativoRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<UsuarioAdministrativoResponse> findAll(
        @org.springframework.security.core.annotation.AuthenticationPrincipal br.com.provas.model.response.LoginResponse usuario) {
        return service.findAll(usuario.id());
    }

    @GetMapping("/{id}")
    public UsuarioAdministrativoResponse findById(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public UsuarioAdministrativoResponse update(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id, @Valid @RequestBody AtualizarUsuarioAdministrativoRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public UsuarioAdministrativoResponse updateStatus(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id, @Valid @RequestBody StatusRequest request) {
        return service.updateStatus(id, request);
    }
}

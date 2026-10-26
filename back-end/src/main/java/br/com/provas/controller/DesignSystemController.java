package br.com.provas.controller;

import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.DesignSystemService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/design-system")
public class DesignSystemController {
    private final DesignSystemService service;

    public DesignSystemController(DesignSystemService service) { this.service = service; }

    @GetMapping("/acesso")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void access(@AuthenticationPrincipal LoginResponse usuario) { service.authorize(usuario); }
}

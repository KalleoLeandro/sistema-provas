package br.com.provas.controller;

import br.com.provas.model.response.LoginResponse;
import br.com.provas.model.response.ProfessorResponse;
import br.com.provas.service.ProfessorService;
import br.com.provas.service.PendenciaQuestoesService;
import br.com.provas.model.response.PendenciaQuestoesResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/professor/dashboard")
public class ProfessorDashboardController {
    private final ProfessorService service;
    private final PendenciaQuestoesService pendencias;
    public ProfessorDashboardController(ProfessorService service, PendenciaQuestoesService pendencias) { this.service = service; this.pendencias = pendencias; }

    @GetMapping
    public ProfessorResponse dashboard(@AuthenticationPrincipal LoginResponse usuario) {
        return service.findById(usuario.id());
    }
    @GetMapping("/pendencias-questoes")
    public List<PendenciaQuestoesResponse> pendingQuestions(@AuthenticationPrincipal LoginResponse usuario) {
        return pendencias.findAll(usuario.id());
    }

}

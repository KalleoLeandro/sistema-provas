package br.com.provas.controller;

import br.com.provas.model.response.AlunoResponse;
import br.com.provas.model.response.PaginaResponse;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.AlunoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/professor/alunos")
public class AlunoController {
    private final AlunoService service;
    public AlunoController(AlunoService service) { this.service=service; }
    @GetMapping
    public PaginaResponse<AlunoResponse> findAll(@AuthenticationPrincipal LoginResponse usuario,
        @RequestParam(defaultValue="1") @Min(value=1,message="A página deve ser positiva.") int pagina,
        @RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho,
        @Valid @ModelAttribute br.com.provas.model.request.AlunoFiltroRequest filtro) {
        return service.search(usuario.id(),filtro,pagina,Integer.parseInt(tamanho));
    }
}

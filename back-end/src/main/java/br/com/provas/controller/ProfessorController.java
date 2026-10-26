package br.com.provas.controller;

import br.com.provas.model.request.ProfessorRequest;
import br.com.provas.model.request.AtualizarProfessorRequest;
import br.com.provas.model.request.StatusRequest;
import br.com.provas.model.response.ProfessorResponse;
import br.com.provas.service.ProfessorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/professores")
public class ProfessorController {
    private final ProfessorService service;
    public ProfessorController(ProfessorService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorResponse create(@Valid @RequestBody ProfessorRequest request) { return service.create(request); }

    @GetMapping
    public List<ProfessorResponse> findAll() { return service.findAll(); }
    @GetMapping("/{id}")
    public ProfessorResponse findById(@PathVariable @Positive(message="Informe um professor válido.") Long id){return service.findById(id);}
    @PutMapping("/{id}")
    public ProfessorResponse update(@PathVariable @Positive(message="Informe um professor válido.") Long id,@Valid @RequestBody AtualizarProfessorRequest request){return service.update(id,request);}

    @PatchMapping("/{id}/status")
    public ProfessorResponse updateStatus(@PathVariable @Positive(message = "O identificador deve ser positivo.") Long id,
            @Valid @RequestBody StatusRequest request) { return service.updateStatus(id, request); }
}

package br.com.provas.controller;

import br.com.provas.model.request.TurmaRequest;
import br.com.provas.model.response.TurmaResponse;
import br.com.provas.service.TurmaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/turmas")
public class TurmaController {
    private final TurmaService service;
    public TurmaController(TurmaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaResponse create(@Valid @RequestBody TurmaRequest request) { return service.create(request); }

    @GetMapping
    public List<TurmaResponse> findAll() { return service.findAll(); }
}

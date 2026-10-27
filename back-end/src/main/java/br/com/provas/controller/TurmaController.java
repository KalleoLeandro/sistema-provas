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

    @GetMapping("/{id}")
    public TurmaResponse findById(@PathVariable @jakarta.validation.constraints.Positive(message="Informe uma turma válida.") Long id){return service.findById(id);}
    @PutMapping("/{id}")
    public TurmaResponse update(@PathVariable @jakarta.validation.constraints.Positive(message="Informe uma turma válida.") Long id,@Valid @RequestBody TurmaRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @jakarta.validation.constraints.Positive(message="Informe uma turma válida.") Long id){service.delete(id);}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaResponse create(@Valid @RequestBody TurmaRequest request) { return service.create(request); }

    @GetMapping
    public List<TurmaResponse> findAll() { return service.findAll(); }
}

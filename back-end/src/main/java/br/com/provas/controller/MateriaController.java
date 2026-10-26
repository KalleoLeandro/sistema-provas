package br.com.provas.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import br.com.provas.model.request.MateriaRequest;
import br.com.provas.model.response.MateriaResponse;
import br.com.provas.service.MateriaService;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/materias")
public class MateriaController {
    private final MateriaService service;

    public MateriaController(MateriaService service) { this.service = service; }

    @GetMapping
    public List<MateriaResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public MateriaResponse findById(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public MateriaResponse update(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id, @Valid @RequestBody MateriaRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "O ID deve ser maior que zero.") Long id) { service.delete(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MateriaResponse create(@Valid @RequestBody MateriaRequest request) { return service.create(request); }
}

package br.com.provas.controller;

import br.com.provas.model.request.QuestaoRequest;
import br.com.provas.model.request.AtualizarQuestaoRequest;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.model.response.QuestaoResponse;
import br.com.provas.model.response.QuestaoResumoResponse;
import br.com.provas.model.response.QuestaoDestinoResponse;
import java.util.List;
import br.com.provas.service.QuestaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/questoes")
public class QuestaoController {
    private final QuestaoService service;
    public QuestaoController(QuestaoService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse create(@AuthenticationPrincipal LoginResponse usuario, @Valid @RequestBody QuestaoRequest request) {
        return service.create(usuario.id(), request);
    }

    @PutMapping("/{id}")
    public QuestaoResponse update(@AuthenticationPrincipal LoginResponse usuario,
            @PathVariable @Positive(message = "O identificador deve ser positivo.") Long id,
            @Valid @RequestBody AtualizarQuestaoRequest request) {
        return service.update(id, usuario.id(), request);
    }

    @GetMapping("/{id}")
    public QuestaoResponse findById(@AuthenticationPrincipal LoginResponse usuario,
            @PathVariable @Positive(message = "O identificador deve ser positivo.") Long id) {
        return service.findById(id, usuario.id());
    }

    @GetMapping
    public List<QuestaoResumoResponse> findAll(@AuthenticationPrincipal LoginResponse usuario) {
        return service.findAll(usuario.id());
    }

    @GetMapping("/opcoes")
    public List<QuestaoDestinoResponse> options(@AuthenticationPrincipal LoginResponse usuario) {
        return service.options(usuario.id());
    }
}

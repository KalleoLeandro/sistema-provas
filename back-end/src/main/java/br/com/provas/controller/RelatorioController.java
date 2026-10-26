package br.com.provas.controller;

import br.com.provas.model.entity.Turma;
import br.com.provas.model.response.*;
import br.com.provas.service.RelatorioService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService service;
    public RelatorioController(RelatorioService service){this.service=service;}
    @GetMapping("/turmas") public ResponseEntity<List<Turma>> turmas(@AuthenticationPrincipal LoginResponse usuario){return semCache(service.turmas(usuario));}
    @GetMapping("/alunos") public ResponseEntity<List<AlunoResponse>> alunos(@AuthenticationPrincipal LoginResponse usuario,@RequestParam @Positive(message="Selecione uma turma válida.") Long turmaId){return semCache(service.alunos(usuario,turmaId));}
    @GetMapping("/provas") public ResponseEntity<List<RelatorioProvaResponse>> provas(@AuthenticationPrincipal LoginResponse usuario,@RequestParam @Positive(message="Selecione uma turma válida.") Long turmaId){return semCache(service.provas(usuario,turmaId));}
    @GetMapping("/resultados") public ResponseEntity<List<RelatorioProvaResponse.Resultado>> resultados(@AuthenticationPrincipal LoginResponse usuario,@RequestParam @Positive(message="Selecione uma prova válida.") Long provaId){return semCache(service.resultados(usuario,provaId));}
    private <T> ResponseEntity<T> semCache(T body){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);}
}

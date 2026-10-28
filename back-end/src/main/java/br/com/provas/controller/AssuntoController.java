package br.com.provas.controller;
import br.com.provas.service.AssuntoService;
import br.com.provas.model.request.AssuntoRequest;
import br.com.provas.model.response.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
@RestController @RequestMapping("/professor/assuntos")
public class AssuntoController {
 private final AssuntoService service;public AssuntoController(AssuntoService service){this.service=service;}
 @GetMapping public List<AssuntoResponse> search(@AuthenticationPrincipal LoginResponse usuario,@RequestParam @Positive(message="Selecione uma matéria válida.") Long materiaId,@RequestParam(defaultValue="") @Size(max=120,message="A busca deve ter até 120 caracteres.") String busca){return service.search(usuario.id(),materiaId,busca);}
 @PostMapping public AssuntoResponse create(@AuthenticationPrincipal LoginResponse usuario,@Valid @RequestBody AssuntoRequest request){return service.create(usuario.id(),request);}
}

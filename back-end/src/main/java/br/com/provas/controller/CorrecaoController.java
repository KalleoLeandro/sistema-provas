package br.com.provas.controller;
import br.com.provas.model.request.SalvarCorrecaoProvaRequest;
import br.com.provas.model.response.*;
import br.com.provas.service.CorrecaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/professor/correcoes")
public class CorrecaoController {
 private final CorrecaoService service;public CorrecaoController(CorrecaoService service){this.service=service;}
 @GetMapping public PaginaResponse<ProvaCorrecaoResumoResponse> list(@AuthenticationPrincipal LoginResponse professor,
  @RequestParam(required=false) @Positive Long turmaId,@RequestParam(defaultValue="todas") @Pattern(regexp="todas|pendentes|corrigidas",message="Selecione uma situação válida.") String situacao,
  @RequestParam(defaultValue="1") @Min(1) int pagina,@RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho){return service.findAll(professor.id(),turmaId,situacao,pagina,Integer.parseInt(tamanho));}
 @GetMapping("/{id}") public ProvaCorrecaoResponse get(@AuthenticationPrincipal LoginResponse professor,@PathVariable @Positive Long id){return service.get(professor.id(),id);}
 @PutMapping("/{id}") public ProvaCorrecaoResponse save(@AuthenticationPrincipal LoginResponse professor,@PathVariable @Positive Long id,@Valid @RequestBody SalvarCorrecaoProvaRequest body){return service.save(professor.id(),id,body);}
}

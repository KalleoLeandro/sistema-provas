package br.com.provas.controller;
import br.com.provas.model.request.*;
import br.com.provas.model.response.*;
import br.com.provas.service.ExecucaoProvaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/aluno/provas/{id}")
public class ExecucaoProvaController {
 private final ExecucaoProvaService service;public ExecucaoProvaController(ExecucaoProvaService service){this.service=service;}
 @GetMapping("/regras") public RegrasProvaResponse rules(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id){return service.rules(aluno.id(),id);}
 @PostMapping("/iniciar") public TentativaProvaResponse start(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@Valid @RequestBody IniciarProvaRequest body,HttpServletRequest request){return finish(service.start(aluno.id(),id,body),request);}
 @GetMapping("/tentativa") public TentativaProvaResponse get(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,HttpServletRequest request){return finish(service.get(aluno.id(),id),request);}
 @PutMapping("/questoes/{numero}/resposta") public TentativaProvaResponse save(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@PathVariable @Min(1) @Max(10) int numero,@Valid @RequestBody SalvarRespostaRequest body,HttpServletRequest request){return finish(service.save(aluno.id(),id,numero,body),request);}
 @PostMapping("/submeter") public TentativaProvaResponse submit(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@Valid @RequestBody SubmeterProvaRequest body,HttpServletRequest request){var result=service.submit(aluno.id(),id,body);if("submetida".equals(result.situacao()))logout(request);return result;}
 private TentativaProvaResponse finish(TentativaProvaResponse result,HttpServletRequest request){if(result.enviadaAgora())logout(request);return result;}
 private void logout(HttpServletRequest request){var session=request.getSession(false);if(session!=null)session.invalidate();SecurityContextHolder.clearContext();}
}

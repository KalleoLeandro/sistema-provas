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
 private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(ExecucaoProvaController.class);
 private final ExecucaoProvaService service;
 private final br.com.provas.service.ProvaAlunoService provas;
 public ExecucaoProvaController(ExecucaoProvaService service,br.com.provas.service.ProvaAlunoService provas){this.service=service;this.provas=provas;}
 @GetMapping("/regras") public RegrasProvaResponse rules(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id){return service.rules(aluno.id(),id);}
 @PostMapping("/iniciar") public TentativaProvaResponse start(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@Valid @RequestBody IniciarProvaRequest body,HttpServletRequest request){return finish(service.start(aluno.id(),id,body),request,aluno.id());}
 @GetMapping("/tentativa") public TentativaProvaResponse get(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,HttpServletRequest request){return finish(service.get(aluno.id(),id),request,aluno.id());}
 @PutMapping("/questoes/{numero}/resposta") public TentativaProvaResponse save(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@PathVariable @Min(1) @Max(200) int numero,@Valid @RequestBody SalvarRespostaRequest body,HttpServletRequest request){return finish(service.save(aluno.id(),id,numero,body),request,aluno.id());}
 @PostMapping("/submeter") public TentativaProvaResponse submit(@AuthenticationPrincipal LoginResponse aluno,@PathVariable @Positive Long id,@Valid @RequestBody SubmeterProvaRequest body,HttpServletRequest request){var result=service.submit(aluno.id(),id,body);return "submetida".equals(result.situacao())?delivered(result,request,aluno.id()):result;}
 private TentativaProvaResponse finish(TentativaProvaResponse result,HttpServletRequest request,Long aluno){return result.enviadaAgora()?delivered(result,request,aluno):result;}
 private TentativaProvaResponse delivered(TentativaProvaResponse result,HttpServletRequest request,Long aluno){
  try{result=result.withProgresso(provas.progresso(aluno));}
  catch(RuntimeException e){LOG.warn("Entrega confirmada, mas não foi possível consultar o progresso das provas.",e);}
  finally{logout(request);}
  return result;
 }
 private void logout(HttpServletRequest request){var session=request.getSession(false);if(session!=null)session.invalidate();SecurityContextHolder.clearContext();}
}

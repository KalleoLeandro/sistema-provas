package br.com.provas.controller;

import br.com.provas.model.request.LiberarProvaRequest;
import br.com.provas.model.response.*;
import br.com.provas.service.ProvaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/professor/provas")
public class ProvaController {
    private final ProvaService service;
    public ProvaController(ProvaService service) { this.service=service; }
    @GetMapping("/turmas/{turmaId}/assuntos")
    public java.util.List<AssuntoProvaResponse> subjectOptions(@AuthenticationPrincipal LoginResponse usuario,@PathVariable @Positive Long turmaId,@RequestParam @Positive Long materiaId){return service.subjectOptions(usuario.id(),turmaId,materiaId);}
    @GetMapping("/turmas/{turmaId}")
    public PaginaResponse<LiberacaoProvaResponse> findAll(@AuthenticationPrincipal LoginResponse usuario,
        @PathVariable @Positive(message="Selecione uma turma válida.") Long turmaId,
        @RequestParam(defaultValue="1") @Min(value=1,message="A página deve ser positiva.") int pagina,
        @RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho,@jakarta.validation.Valid @ModelAttribute br.com.provas.model.request.OrdenacaoRequest order){
        return service.findAll(usuario.id(),turmaId,pagina,Integer.parseInt(tamanho),order);
    }
    @PostMapping("/turmas/{turmaId}/liberacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public LiberacaoProvaResponse release(@AuthenticationPrincipal LoginResponse usuario,
        @PathVariable @Positive(message="Selecione uma turma válida.") Long turmaId,
        @Valid @RequestBody LiberarProvaRequest request) { return service.release(usuario.id(),turmaId,request); }
    @GetMapping("/alunos/{alunoId}")
    public PaginaResponse<LiberacaoProvaResponse> findByStudent(@AuthenticationPrincipal LoginResponse usuario,
        @PathVariable @Positive(message="Selecione um aluno válido.") Long alunoId,
        @RequestParam(defaultValue="1") @Min(value=1,message="A página deve ser positiva.") int pagina,
        @RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho,@jakarta.validation.Valid @ModelAttribute br.com.provas.model.request.OrdenacaoRequest order){
        return service.findByStudent(usuario.id(),alunoId,pagina,Integer.parseInt(tamanho),order);
    }
    @PutMapping("/turmas/{turmaId}/liberacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal LoginResponse usuario,@PathVariable @Positive Long turmaId,@PathVariable @Positive Long id,
      @Valid @RequestBody LiberarProvaRequest request){service.update(usuario.id(),turmaId,id,request);}
    @GetMapping("/turmas/{turmaId}/liberacoes/{id}")
    public LiberacaoDetalheResponse getForEdit(@AuthenticationPrincipal LoginResponse usuario,@PathVariable @Positive Long turmaId,@PathVariable @Positive Long id,jakarta.servlet.http.HttpServletResponse response){response.setHeader("Cache-Control","no-store");return service.getForEdit(usuario.id(),turmaId,id);}
}

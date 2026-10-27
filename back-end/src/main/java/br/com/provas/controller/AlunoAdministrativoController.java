package br.com.provas.controller;

import br.com.provas.model.request.AlunoRequest;
import br.com.provas.model.request.ImportarAlunosRequest;
import br.com.provas.model.response.ImportacaoAlunosResponse;
import br.com.provas.service.AlunoImportacaoService;
import org.springframework.http.ResponseEntity;
import br.com.provas.model.response.AlunoResponse;
import br.com.provas.model.response.PaginaResponse;
import br.com.provas.service.AlunoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alunos")
public class AlunoAdministrativoController {
    private final AlunoService service;
    private final AlunoImportacaoService importacao;
    public AlunoAdministrativoController(AlunoService service,AlunoImportacaoService importacao){this.service=service;this.importacao=importacao;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse create(@Valid @RequestBody AlunoRequest request){return service.create(request);}
    @GetMapping("/{id}")
    public AlunoResponse findById(@PathVariable @Positive(message="Informe um aluno válido.") Long id){return service.findById(id);}
    @PutMapping("/{id}")
    public AlunoResponse update(@PathVariable @Positive(message="Informe um aluno válido.") Long id,@Valid @RequestBody AlunoRequest request){return service.update(id,request);}
    @GetMapping
    public PaginaResponse<AlunoResponse> findAll(
        @Valid @ModelAttribute br.com.provas.model.request.AlunoFiltroRequest filtro,
        @RequestParam(defaultValue="1") @Min(value=1,message="A página deve ser positiva.") int pagina,
        @RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho){
        return service.search(null,filtro,pagina,Integer.parseInt(tamanho));
    }
    @PostMapping("/importar/validar")
    public ImportacaoAlunosResponse validateCsv(@Valid @RequestBody ImportarAlunosRequest request){return importacao.validate(request);}
    @PostMapping("/importar")
    public ResponseEntity<ImportacaoAlunosResponse> importCsv(@Valid @RequestBody ImportarAlunosRequest request){
        var result=importacao.importCsv(request);
        return ResponseEntity.status(result.erros().isEmpty()?HttpStatus.CREATED:HttpStatus.BAD_REQUEST).body(result);
    }

}

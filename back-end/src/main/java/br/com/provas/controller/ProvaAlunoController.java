package br.com.provas.controller;
import br.com.provas.model.response.*;
import br.com.provas.service.ProvaAlunoService;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/aluno/provas")
public class ProvaAlunoController {
    private final ProvaAlunoService service;
    public ProvaAlunoController(ProvaAlunoService service){this.service=service;}
    @GetMapping public ProvasAlunoResponse findAvailable(@AuthenticationPrincipal LoginResponse aluno,
        @RequestParam(defaultValue="1") @Min(value=1,message="A página deve ser positiva.") int pagina,
        @RequestParam(defaultValue="15") @Pattern(regexp="15|25|50",message="Selecione 15, 25 ou 50 itens por página.") String tamanho,@jakarta.validation.Valid @ModelAttribute br.com.provas.model.request.OrdenacaoRequest order){
        return service.findAvailable(aluno.id(),pagina,Integer.parseInt(tamanho),order);
    }
}

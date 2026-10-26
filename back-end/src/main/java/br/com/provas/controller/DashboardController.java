package br.com.provas.controller;

import br.com.provas.model.response.DashboardIndicadoresResponse;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.service.DashboardService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service){this.service=service;}
    @GetMapping("/indicadores")
    public ResponseEntity<DashboardIndicadoresResponse> indicadores(@AuthenticationPrincipal LoginResponse usuario){
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.indicadores(usuario));
    }
}

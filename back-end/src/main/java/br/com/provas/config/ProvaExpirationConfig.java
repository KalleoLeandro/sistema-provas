package br.com.provas.config;
import br.com.provas.repository.ExecucaoProvaRepository;
import br.com.provas.service.ExecucaoProvaService;
import java.time.Clock;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
@Configuration @EnableScheduling
public class ProvaExpirationConfig {
 private final ExecucaoProvaRepository repository;private final ExecucaoProvaService service;private final Clock clock;
 public ProvaExpirationConfig(ExecucaoProvaRepository repository,ExecucaoProvaService service,Clock clock){this.repository=repository;this.service=service;this.clock=clock;}
 @Scheduled(fixedDelay=15000,initialDelay=15000) public void expire(){for(Long aluno:repository.expiredStudents(clock.instant()))service.expireForStudent(aluno);}
}

package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.model.response.RelatorioProvaResponse;
import br.com.provas.repository.RelatorioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RelatorioServiceTest {
    private final RelatorioRepository repository=mock(RelatorioRepository.class);
    private final RelatorioService service=new RelatorioService(repository);
    private final LoginResponse professor=new LoginResponse(21L,"Professor","professor","professor");
    @Test void administradorConsultaEscopoCompleto(){service.turmas(new LoginResponse(1L,"Admin","admin","administrador"));verify(repository).turmas(null);}
    @Test void professorConsultaSomentePropriosVinculos(){service.turmas(professor);verify(repository).turmas(21L);}
    @Test void turmaDeOutroProfessorNaoPodeSerImpressa(){assertThrows(DefaultErrorException.class,()->service.alunos(professor,13L));verify(repository,never()).alunos(any(),any());}
    @Test void provaDeOutroProfessorNaoRevelaResultados(){assertThrows(DefaultErrorException.class,()->service.resultados(professor,4L));verify(repository,never()).resultados(any(),any());}
    @Test void alunoNaoConsultaNotasAntesDaPublicacao(){assertThrows(DefaultErrorException.class,()->service.resultados(new LoginResponse(72L,"Ana","001","aluno"),4L));verifyNoInteractions(repository);}
    @Test void consultaCompletaDaProvaExigeAutoria(){when(repository.prova(21L,4L)).thenReturn(Optional.of(new RelatorioProvaResponse(4L,"Prova","Matemática","Professor",5L,null,null)));service.resultados(professor,4L);verify(repository).resultados(21L,4L);}
}

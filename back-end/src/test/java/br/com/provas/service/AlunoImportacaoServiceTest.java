package br.com.provas.service;

import br.com.provas.model.entity.Turma;
import br.com.provas.model.request.ImportarAlunosRequest;
import br.com.provas.repository.AlunoRepository;
import br.com.provas.exceptions.DefaultErrorException;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlunoImportacaoServiceTest {
    private final AlunoRepository repository=mock(AlunoRepository.class);
    private final jakarta.validation.ValidatorFactory factory=Validation.buildDefaultValidatorFactory();
    private final AlunoImportacaoService service=new AlunoImportacaoService(repository,factory.getValidator());
    private static final String HEADER="nome;numero;cgm;status;ensino;ano;letra\n";
    @BeforeEach void setup(){
        when(repository.findImportTurmas()).thenReturn(List.of(new Turma(5L,"F",1,"A"),new Turma(13L,"F",1,"B")));
        when(repository.findExistingCgms(anyList())).thenReturn(Set.of());when(repository.turmaExists(anyLong())).thenReturn(true);
    }
    @AfterEach void close(){factory.close();}
    @Test void validatesWithoutWritingAndImportsAllValidRows(){
        var request=new ImportarAlunosRequest(HEADER+"Ana;;00123;M;F;1;A\nBia;12;00124;Transferido;Fundamental;1;B\n");
        var preview=service.validate(request);assertEquals(2,preview.validos());assertTrue(preview.erros().isEmpty());assertEquals(0,preview.importados());
        assertNull(preview.linhas().getFirst().numero());assertEquals("00123",preview.linhas().getFirst().cgm());assertEquals("T",preview.linhas().get(1).status());
        verify(repository,never()).saveAll(anyList());
        assertEquals(2,service.importCsv(request).importados());verify(repository).saveAll(argThat(list->list.size()==2));
    }
    @Test void invalidRowsStopTheWholeBatchAndIdentifyPhysicalLineAndField(){
        var result=service.importCsv(new ImportarAlunosRequest(HEADER+"Ana;;001;M;F;1;A\nBia;1.5;002;X;F;1;B\n"));
        assertEquals(1,result.validos());assertEquals(0,result.importados());
        assertTrue(result.erros().stream().anyMatch(e->e.linha()==3&&e.campo().equals("numero")));
        assertTrue(result.erros().stream().anyMatch(e->e.linha()==3&&e.campo().equals("status")));verify(repository,never()).saveAll(anyList());
    }
    @Test void rejectsDuplicateCgmsAndUnknownClasses(){
        when(repository.findExistingCgms(anyList())).thenReturn(Set.of("001"));
        var r=service.importCsv(new ImportarAlunosRequest(HEADER+"Ana;;001;M;F;1;A\nBia;;001;M;F;9;Z\n"));
        assertTrue(r.erros().stream().anyMatch(e->e.campo().equals("turma")));
        assertTrue(r.erros().stream().anyMatch(e->e.mensagem().equals("CGM repetido no arquivo.")));
        assertTrue(r.erros().stream().anyMatch(e->e.mensagem().equals("CGM já cadastrado.")));verify(repository,never()).saveAll(anyList());
    }
    @Test void numberColumnCanBeAbsentButRequiredHeadersCannot(){
        var r=service.validate(new ImportarAlunosRequest("nome,cgm,status,ensino,ano,letra\nAna,001,M,F,1,A\n"));assertEquals(1,r.validos());
        var invalid=service.validate(new ImportarAlunosRequest("nome;cgm\nAna;001"));assertTrue(invalid.erros().stream().anyMatch(e->e.campo().equals("cabecalho")));
    }
    @Test void concurrentDuplicateIsNotSilentlyImported(){
        doThrow(new org.springframework.dao.DuplicateKeyException("duplicate")).when(repository).saveAll(anyList());
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.importCsv(new ImportarAlunosRequest(HEADER+"Ana;;001;M;F;1;A"))).getStatus());
    }
}

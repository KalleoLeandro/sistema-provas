package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.*;
import br.com.provas.repository.ProvaAlunoRepository;
import java.time.Clock;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta as provas disponíveis ao aluno autenticado no ano letivo atual. */
@Service
public class ProvaAlunoService {
    private final ProvaAlunoRepository repository;
    private final Clock clock;
    private final ExecucaoProvaService execution;
    public ProvaAlunoService(ProvaAlunoRepository repository,Clock clock,ExecucaoProvaService execution){this.repository=repository;this.clock=clock;this.execution=execution;}
    /**
     * Consulta o progresso após a confirmação persistida da entrega.
     * @param alunoId identidade obtida da sessão que será encerrada
     * @return totais com o mesmo escopo da lista de provas, incluindo a entrega atual
     */
    @Transactional(readOnly=true)
    public ProgressoProvasResponse progresso(Long alunoId){
        /* A entrega já foi confirmada antes desta consulta. Não inicia provas nem altera outras tentativas;
           conta todas as páginas e usa o mesmo instante para os totais, sem expor notas ao aluno. */
        var agora=clock.instant();
        return new ProgressoProvasResponse(repository.countAvailable(alunoId,agora),repository.countCompleted(alunoId,agora));
    }
    /**
     * Lista liberações vigentes e resume o progresso do aluno.
     * @param alunoId identidade obtida exclusivamente da sessão
     * @param pagina página iniciando em um
     * @param tamanho limite de 15, 25 ou 50
     * @return lista paginada e totais independentes da página atual
     */
    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public ProvasAlunoResponse findAvailable(Long alunoId,int pagina,int tamanho){
        /* O intervalo é validado no servidor com o mesmo instante nas duas consultas. */
        if(pagina<1 || !Set.of(15,25,50).contains(tamanho))throw new DefaultErrorException("Informe página positiva e 15, 25 ou 50 itens por página.");
        execution.expireForStudent(alunoId);
        var agora=clock.instant();long total=repository.countAvailable(alunoId,agora);
        int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
        var provas=new PaginaResponse<>(repository.findAvailable(alunoId,agora,tamanho,(long)(atual-1)*tamanho),total,atual,tamanho);
        /* O progresso usa entregas persistidas, incluindo provas encerradas automaticamente. */
        return new ProvasAlunoResponse(provas,total,repository.countCompleted(alunoId,agora));
    }
}

package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.DashboardIndicadoresResponse;
import br.com.provas.model.response.LoginResponse;
import br.com.provas.repository.DashboardRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consolida desempenho e participação sem disponibilizar resultados aos alunos. */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository repository;
    private final Clock clock;

    /**
     * Consulta indicadores das avaliações já disponibilizadas para alunos matriculados e remanejados.
     * @param usuario identidade autenticada; o escopo nunca é recebido por parâmetro da tela
     * @return alunos distintos, médias concluídas e contagem de avaliações por situação
     */
    @Transactional(readOnly=true)
    public DashboardIndicadoresResponse indicadores(LoginResponse usuario){
        /* Administração abrange a escola; professor considera apenas suas provas e vínculos ativos. */
        Long professor;
        if("administrador".equals(usuario.perfil()))professor=null;
        else if("professor".equals(usuario.perfil()))professor=usuario.id();
        else throw new DefaultErrorException("Acesso não permitido ao dashboard.",HttpStatus.FORBIDDEN);

        /* Transferidos e desistentes são excluídos na base comum dos cards, médias e gráficos,
           mesmo que possuam provas entregues. Matriculados e remanejados permanecem no cálculo. */
        /* Usa o mesmo instante e a mesma transação nas consultas para manter cards e gráficos coerentes. */
        var agora=clock.instant();var resumo=repository.resumo(professor,agora);
        var medias=repository.medias(professor,agora);

        /* Médias excluem discursivas pendentes; ausência de correções permanece nula, sem simular nota zero.
           Um aluno que entregou uma prova e ainda deve outra pertence aos dois cards de participação. */
        return new DashboardIndicadoresResponse(resumo.alunosFizeram(),resumo.alunosPendentes(),resumo.mediaGeral(),
            resumo.naoIniciadas(),resumo.emAndamento(),resumo.aguardandoCorrecao(),resumo.corrigidas(),medias);
    }
}

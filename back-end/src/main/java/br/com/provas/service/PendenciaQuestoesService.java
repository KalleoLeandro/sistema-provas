package br.com.provas.service;

import br.com.provas.model.response.PendenciaQuestoesResponse;
import br.com.provas.model.response.QuantidadeQuestoesResponse;
import br.com.provas.repository.QuestaoRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Identifica insuficiência de questões nas matérias e turmas vinculadas ao professor autenticado. */
@Service
public class PendenciaQuestoesService {
    private final QuestaoRepository repository;
    public PendenciaQuestoesService(QuestaoRepository repository) { this.repository = repository; }

    /**
     * Calcula as quantidades mínimas por peso para cada matéria/turma ativa do professor.
     * @param professorId identidade extraída da sessão pelo controller
     * @return somente combinações que ainda não atingiram todos os mínimos
     */
    @Transactional(readOnly = true)
    public List<PendenciaQuestoesResponse> findAll(Long professorId) {
        /* As turmas seguem o mesmo escopo do cadastro: apenas vínculos ativos do professor na matéria.
         * A consulta conta apenas questões atuais do próprio professor. Histórico não aumenta o estoque.
         * Questões do ano inteiro contam em cada turma desse ensino/ano; as específicas só nos vínculos escolhidos. */
        return repository.countByAssignedClass(professorId).stream().map(q -> {
            /* As metas são independentes: excesso em um peso não compensa falta em outro.
             * O tipo (objetiva ou discursiva) não altera a contagem, pois a regra exige apenas o peso. */
            var quantidades = List.of(quantity("0.5",4,q.peso05()), quantity("1.0",3,q.peso10()),
                quantity("1.5",2,q.peso15()), quantity("2.0",1,q.peso20()));
            return new PendenciaQuestoesResponse(q.materiaId(),q.materia(),q.turmaId(),q.ensino(),q.ano(),q.letra(),quantidades);
        }).filter(p -> p.quantidades().stream().anyMatch(q -> q.faltantes()>0)).toList();
    }

    /** @param peso peso da questão @param minimo meta por turma @param cadastradas estoque atual @return progresso e déficit */
    private QuantidadeQuestoesResponse quantity(String peso, int minimo, int cadastradas) {
        /* O déficit nunca é negativo, mesmo quando há mais questões do que o mínimo. */
        return new QuantidadeQuestoesResponse(new BigDecimal(peso),minimo,cadastradas,Math.max(0,minimo-cadastradas));
    }
}

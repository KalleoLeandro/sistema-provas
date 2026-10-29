package br.com.provas.service;
import br.com.provas.model.response.*;import br.com.provas.repository.QuestaoRepository;import java.util.List;
import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
/** Identifica faltas no estoque usado pelas distribuições já liberadas. */
@Service
public class PendenciaQuestoesService {
 private final QuestaoRepository repository;public PendenciaQuestoesService(QuestaoRepository repository){this.repository=repository;}
 /** Compara o estoque atual com cada composição persistida, sem mínimos fixos por peso.
  * @param professorId professor autenticado @return faltas por prova, assunto e peso
  */
 @Transactional(readOnly=true)
 public List<PendenciaQuestoesResponse> findAll(Long professorId){
  /* Uma edição posterior de questão pode reduzir o estoque de uma prova; esta consulta avisa
   * o professor sem alterar tentativas iniciadas. Sem liberação não há mínimo genérico a exigir. */
  return repository.countMissingPlans(professorId).stream().map(q->new PendenciaQuestoesResponse(q.materiaId(),q.materia(),q.turmaId(),q.ensino(),q.ano(),q.letra(),List.of(new QuantidadeQuestoesResponse(q.peso(),q.minimo(),q.cadastradas(),Math.max(0,q.minimo()-q.cadastradas()))),q.assuntoId(),q.assunto(),q.liberacaoId(),q.titulo())).toList();
 }
}

package br.com.provas.service;
import br.com.provas.model.entity.EstadoProvaRedis;
import br.com.provas.repository.ExecucaoProvaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Consolida no MySQL uma entrega já selada de forma atômica no Redis. */
@Service
public class EntregaProvaService {
 private final ExecucaoProvaRepository repository;public EntregaProvaService(ExecucaoProvaRepository repository){this.repository=repository;}
 /**
  * Persiste respostas e pontuação interna uma única vez, sem expor resultados ao aluno.
  * @param aluno proprietário autenticado
  * @param lib liberação
  * @param state respostas congeladas pelo Redis
  * @return se esta operação realizou a primeira entrega
  */
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
 public boolean persist(Long aluno,Long lib,EstadoProvaRedis state){
  /* O único bloqueio de linha por tentativa acontece na consolidação final, nunca a cada marcação. */
  var attempt=repository.lockForSubmission(aluno,lib).orElseThrow();if(attempt.submetidaEm()!=null)return false;
  /* A transação confirma todas as respostas e a entrega juntas; falhas permitem nova tentativa sem perder o Redis. */
  repository.persistAnswers(state);repository.submit(attempt.id(),state.enviadaEm(),state.automatica());return true;
 }
}

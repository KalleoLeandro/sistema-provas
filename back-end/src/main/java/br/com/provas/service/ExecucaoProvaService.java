package br.com.provas.service;
import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.*;
import br.com.provas.model.request.*;
import br.com.provas.model.response.*;
import br.com.provas.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;

/** Mantém cópia fixa no MySQL e usa Redis durável para respostas, revisões e concorrência. */
@Service
public class ExecucaoProvaService {
 private final ExecucaoProvaRepository repository;private final ProvaRedisRepository redis;private final EntregaProvaService delivery;
 private final Clock clock;private final PasswordEncoder encoder;
 public ExecucaoProvaService(ExecucaoProvaRepository repository,Clock clock,PasswordEncoder encoder,ProvaRedisRepository redis,EntregaProvaService delivery){this.repository=repository;this.clock=clock;this.encoder=encoder;this.redis=redis;this.delivery=delivery;}
 /**
  * Consulta regras sem iniciar cronômetro nem revelar questões/token.
  * @param aluno identidade autenticada
  * @param lib liberação
  * @return configuração pública
  */
 @Transactional(readOnly=true)
 public RegrasProvaResponse rules(Long aluno,Long lib){
  /* Tentativas já existentes mantêm seus próprios título e prazo, mesmo após edição da liberação. */
  var existing=repository.findAttempt(aluno,lib);if(existing.isPresent()){var a=existing.get();return new RegrasProvaResponse(lib,a.titulo(),a.materia(),(int)Math.max(1,Duration.between(a.iniciadaEm(),a.encerraEm()).toMinutes()),a.encerraEm(),true,a.submetidaEm()!=null,repository.attemptCount(a.id()),repository.attemptValue(a.id()));}
  var c=available(aluno,lib,clock.instant());return new RegrasProvaResponse(lib,c.titulo(),c.materia(),c.duracaoMinutos(),c.encerraEm(),false,false,c.quantidadeQuestoes(),c.valorTotal());
 }
 /**
  * Sorteia uma vez e persiste o conteúdo; o bloqueio de início ocorre no Redis por aluno/liberação.
  * @param aluno proprietário
  * @param lib liberação
  * @param request token e aceite
  * @return tentativa pública
  */
 @Transactional
 public TentativaProvaResponse start(Long aluno,Long lib,IniciarProvaRequest request){
  /* A trava tem proprietário e expiração; a chave única no MySQL também impede duplicação. */
  String lease=redis.lockStart(aluno,lib);boolean synchronizedTx=TransactionSynchronizationManager.isSynchronizationActive();
  if(synchronizedTx)TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCompletion(int status){redis.unlockStart(aluno,lib,lease);}});
  try{
   if(repository.findAttempt(aluno,lib).isPresent())return get(aluno,lib);
   if(!Boolean.TRUE.equals(request.aceitouRegras()))throw new DefaultErrorException("Confirme a leitura das regras.");
   var c=available(aluno,lib,clock.instant(),true);if(c.tokenHash()==null||request.token()==null||!encoder.matches(request.token(),c.tokenHash()))throw new DefaultErrorException("Token de início inválido. Confira com o professor.",HttpStatus.FORBIDDEN);
   var selected=QuestionDrawService.draw(repository.candidates(c),c.composicao());Instant now=clock.instant();
   if(!now.isBefore(c.encerraEm())||!redis.ownsStart(aluno,lib,lease))throw new DefaultErrorException("Não foi possível confirmar o início. Tente novamente.",HttpStatus.CONFLICT);
   Instant end=now.plus(Duration.ofMinutes(c.duracaoMinutos()));if(end.isAfter(c.encerraEm()))end=c.encerraEm();
   var attempt=repository.create(aluno,c,now,end,selected);
   var result=new TentativaProvaResponse(lib,attempt.titulo(),attempt.materia(),attempt.iniciadaEm(),attempt.encerraEm(),now,"em_andamento",false,repository.questions(attempt.id()));
   /* Publica o estado no Redis somente depois do commit; uma falha posterior retoma a cópia já gravada, sem novo sorteio. */
   if(synchronizedTx)TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){@Override public void afterCommit(){redis.initialize(attempt,result);}});else redis.initialize(attempt,result);
   return result;
  }finally{if(!synchronizedTx)redis.unlockStart(aluno,lib,lease);}
 }
 /**
  * Retoma conteúdo e respostas do Redis, recuperando a cópia do MySQL apenas em acesso frio.
  * @param aluno proprietário
  * @param lib liberação
  * @return prova atual; provas vencidas são seladas e entregues antes da resposta
  */
 public TentativaProvaResponse get(Long aluno,Long lib){
  var state=state(aluno,lib);
  /* O relógio Redis também valida o prazo atomicamente; o navegador nunca amplia a duração. */
  if(!state.selada()&&!clock.instant().isBefore(state.copia().encerraEm())){redis.seal(aluno,lib,true,true);state=state(aluno,lib);}
  return response(aluno,lib,state);
 }
 /**
  * Grava somente no Redis, sem bloqueio nem atualização de respostas no MySQL a cada marcação.
  * @param aluno proprietário
  * @param lib liberação
  * @param number questão fixa
  * @param request resposta e revisão
  * @return respostas confirmadas pelo servidor
  */
 public TentativaProvaResponse save(Long aluno,Long lib,int number,SalvarRespostaRequest request){
  var state=state(aluno,lib);if(state.selada())return response(aluno,lib,state);
  var question=state.copia().questoes().stream().filter(q->q.numero()==number).findFirst().orElseThrow(()->new DefaultErrorException("Questão não encontrada.",HttpStatus.NOT_FOUND));
  String answer=request.resposta()==null||request.resposta().isBlank()?null:request.resposta();
  if("objetiva".equals(question.tipo())&&answer!=null&&!question.alternativas().containsKey(answer))throw new DefaultErrorException("Selecione uma alternativa válida.");
  /* Script Lua faz comparação de revisão, verificação de tempo e gravação em uma única operação. */
  long result=redis.answer(aluno,lib,number,answer,request.revisao());
  if(result==-1)throw new DefaultErrorException("A resposta foi alterada em outra aba. Recarregue a prova antes de continuar.",HttpStatus.CONFLICT);
  if(result==-2)redis.seal(aluno,lib,true,true);
  return response(aluno,lib,state(aluno,lib));
 }
 /**
  * Congela respostas antes da consolidação final; não aceita alterações após o selo.
  * @param aluno proprietário
  * @param lib liberação
  * @param request confirmação de questões em branco
  * @return entrega sem nota ou gabarito
  */
 public TentativaProvaResponse submit(Long aluno,Long lib,SubmeterProvaRequest request){
  state(aluno,lib);
  /* A conferência de respostas faltantes e o congelamento são atômicos, inclusive entre várias abas. */
  long result=redis.seal(aluno,lib,Boolean.TRUE.equals(request.confirmarIncompleta()),false);
  if(result==-4)throw new DefaultErrorException("Há questões sem resposta. Confirme o envio incompleto.",HttpStatus.CONFLICT);
  return response(aluno,lib,state(aluno,lib));
 }
 /**
  * Finaliza vencidas na consulta da lista e na rotina periódica, inclusive com navegador fechado.
  * @param aluno proprietário
  */
 public void expireForStudent(Long aluno){for(Long lib:repository.expiredReleases(aluno,clock.instant()))get(aluno,lib);}
 /** @param aluno proprietário @param lib liberação @return estado durável de respostas */
 private EstadoProvaRedis state(Long aluno,Long lib){
  /* Redis indisponível interrompe o salvamento: não há fallback que descarte respostas ainda não consolidadas. */
  var cached=redis.find(aluno,lib);if(cached.isPresent())return cached.get();
  var attempt=repository.findAttempt(aluno,lib).orElseThrow(()->new DefaultErrorException("Prova não iniciada.",HttpStatus.NOT_FOUND));
  var copy=new TentativaProvaResponse(lib,attempt.titulo(),attempt.materia(),attempt.iniciadaEm(),attempt.encerraEm(),clock.instant(),attempt.submetidaEm()==null?"em_andamento":"submetida",false,repository.questions(attempt.id()));
  redis.initialize(attempt,copy);return redis.find(aluno,lib).orElseThrow();
 }
 /** @param aluno proprietário @param lib liberação @param state estado confirmado @return campos públicos, sem pontuação nem respostas corretas */
 private TentativaProvaResponse response(Long aluno,Long lib,EstadoProvaRedis state){
  /* Se MySQL falhar, o selo e as respostas permanecem no Redis para repetir a entrega com segurança. */
  boolean submittedNow=false;if(state.selada()&&!state.persistida()){submittedNow=delivery.persist(aluno,lib,state);redis.finished(aluno,lib);}
  var questions=state.copia().questoes().stream().map(q->new QuestaoExecucaoResponse(q.numero(),q.tipo(),q.peso(),q.enunciado(),q.alternativas(),state.respostas().get(q.numero()),state.revisoes().get(q.numero()))).toList();
  var copy=state.copia();return new TentativaProvaResponse(lib,copy.titulo(),copy.materia(),copy.iniciadaEm(),copy.encerraEm(),clock.instant(),state.selada()?"submetida":"em_andamento",submittedNow,questions);
 }
 /** @param aluno identidade autenticada @param lib liberação @param now instante atual @return configuração autorizada */
 private ConfiguracaoInicioProva available(Long aluno,Long lib,Instant now){return available(aluno,lib,now,false);}
 private ConfiguracaoInicioProva available(Long aluno,Long lib,Instant now,boolean lock){
  /* O cadastro define turma, matéria e professor; IDs fornecidos pelo aluno não ampliam seu acesso. */
  /* O início bloqueia a configuração para congelar também a seleção de assuntos durante o sorteio. */
  var c=(lock?repository.configuration(aluno,lib,true):repository.configuration(aluno,lib)).orElseThrow(()->new DefaultErrorException("Prova não encontrada.",HttpStatus.NOT_FOUND));
  if(now.isBefore(c.disponivelEm())||!now.isBefore(c.encerraEm()))throw new DefaultErrorException("Esta prova não está disponível para iniciar.",HttpStatus.CONFLICT);return c;
 }
}

package br.com.provas.service;
import br.com.provas.model.entity.QuestaoSorteio;import br.com.provas.model.request.ComposicaoProvaRequest;
import br.com.provas.exceptions.DefaultErrorException;import org.springframework.http.HttpStatus;import java.util.*;import java.security.SecureRandom;
/** Sorteia questões a partir da distribuição conferida e persistida pelo professor. */
public final class QuestionDrawService {
 private static final SecureRandom RANDOM=new SecureRandom();private QuestionDrawService(){}
 /** Seleciona itens distintos por assunto/peso e embaralha toda a sequência.
  * @param candidates questões atuais autorizadas @param plan quantidades já conferidas
  * @return conteúdo que será congelado na tentativa do aluno
  */
 public static List<QuestaoSorteio> draw(List<QuestaoSorteio> candidates,List<ComposicaoProvaRequest> plan){
  /* Não refaz a distribuição silenciosamente se o banco mudou após a liberação. */
  if(plan==null||plan.isEmpty())throw new DefaultErrorException("A configuração desta prova precisa ser atualizada pelo professor.",HttpStatus.CONFLICT);
  var result=new ArrayList<QuestaoSorteio>();var ids=new HashSet<Long>();var errors=new ArrayList<String>();
  for(var group:plan){var pool=new ArrayList<>(candidates.stream().filter(q->group.assuntoId().equals(q.assuntoId())&&group.peso().compareTo(q.peso())==0).toList());
   if(pool.size()<group.quantidade()){errors.add("peso "+group.peso().toPlainString().replace('.',',')+": faltam "+(group.quantidade()-pool.size()));continue;}
   Collections.shuffle(pool,RANDOM);for(var q:pool.subList(0,group.quantidade())){if(!ids.add(q.id()))throw new DefaultErrorException("A distribuição contém questões repetidas.",HttpStatus.CONFLICT);result.add(q);}
  }
  if(!errors.isEmpty())throw new DefaultErrorException("Não há questões suficientes: "+String.join("; ",errors)+". Avise o professor.",HttpStatus.CONFLICT);
  if(result.size()<3||result.size()>200)throw new DefaultErrorException("A prova deve ter de 3 a 200 questões.",HttpStatus.CONFLICT);
  /* Não há posições reservadas por peso: a regra variável usa embaralhamento completo. */
  Collections.shuffle(result,RANDOM);return List.copyOf(result);
 }
}

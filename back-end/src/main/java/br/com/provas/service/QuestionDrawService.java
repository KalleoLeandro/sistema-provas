package br.com.provas.service;
import br.com.provas.model.entity.QuestaoSorteio;
import br.com.provas.exceptions.DefaultErrorException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.security.SecureRandom;
import java.math.BigDecimal;
/** Regras puras compartilhadas pela liberação e pelo início da prova. */
public final class QuestionDrawService {
 private static final SecureRandom RANDOM=new SecureRandom();
 private static final int[] WEIGHTS={5,10,15,20},COUNTS={4,3,2,1};
 private QuestionDrawService(){}
 /** Valida os pesos, um assunto completo e a cobertura de todos os assuntos selecionados.
  * @param candidates questões atuais do professor, matéria, ano/turma e assuntos
  * @param subjects assuntos explicitamente selecionados
  */
 public static void validate(List<QuestaoSorteio> candidates,List<Long> subjects){
  /* Resolver a cobertura por capacidade evita aceitar combinações impossíveis, como dois
   * assuntos que só possuem questão de peso 2,0 quando há uma única vaga desse peso. */
  prepare(candidates,subjects);
 }
 /** Sorteia dez questões distintas com cobertura de assuntos e posições de peso preservadas.
  * @param candidates banco autorizado e atual
  * @param subjects assuntos da liberação
  * @return cópia pronta para congelamento na tentativa
  */
 public static List<QuestaoSorteio> draw(List<QuestaoSorteio> candidates,List<Long> subjects){
  /* Primeiro reserva uma questão por assunto em vagas compatíveis; depois completa os pesos.
   * A associação usa caminhos aumentantes e não depende de tentativas aleatórias limitadas. */
  var plan=prepare(candidates,subjects);var selected=new ArrayList<QuestaoSorteio>();
  for(int slot=0;slot<plan.owners.length;slot++)if(plan.owners[slot]>=0){final int i=slot;Long subject=plan.subjects.get(plan.owners[slot]);
   var pool=new ArrayList<>(plan.pool.stream().filter(q->subject.equals(q.assuntoId())&&weight(q)==plan.slots[i]).toList());Collections.shuffle(pool,RANDOM);selected.add(pool.getFirst());}
  for(int i=0;i<WEIGHTS.length;i++){final int w=WEIGHTS[i];int needed=COUNTS[i]-(int)selected.stream().filter(q->weight(q)==w).count();
   var pool=new ArrayList<>(plan.pool.stream().filter(q->weight(q)==w&&selected.stream().noneMatch(s->s.id().equals(q.id()))).toList());Collections.shuffle(pool,RANDOM);selected.addAll(pool.subList(0,needed));}
  /* A primeira vale 0,5; a única de 2,0 ocupa aleatoriamente uma posição da sétima à décima. */
  Collections.shuffle(selected,RANDOM);var first=selected.stream().filter(q->weight(q)==5).findFirst().orElseThrow();selected.remove(first);
  var highest=selected.stream().filter(q->weight(q)==20).findFirst().orElseThrow();selected.remove(highest);
  var result=new ArrayList<QuestaoSorteio>(Collections.nCopies(10,null));result.set(0,first);result.set(6+RANDOM.nextInt(4),highest);
  var iterator=selected.iterator();for(int i=0;i<10;i++)if(result.get(i)==null)result.set(i,iterator.next());return List.copyOf(result);
 }
 /** @param questions questões de um único assunto @return se atende aos quatro mínimos */
 public static boolean complete(List<QuestaoSorteio> questions){
  /* Excesso em um peso não compensa falta em outro. */
  for(int i=0;i<WEIGHTS.length;i++){final int w=WEIGHTS[i];if(questions.stream().filter(q->weight(q)==w).count()<COUNTS[i])return false;}return true;
 }
 private record Plan(List<QuestaoSorteio> pool,List<Long> subjects,int[] slots,int[] owners){}
 /** @param candidates banco autorizado @param subjects seleção obrigatória @return associação válida entre assuntos e vagas */
 private static Plan prepare(List<QuestaoSorteio> candidates,List<Long> subjects){
  /* Valores livres, repetidos ou vazios não podem liberar um sorteio genérico. */
  if(subjects==null||subjects.isEmpty()||subjects.size()>10||subjects.stream().anyMatch(Objects::isNull)||new HashSet<>(subjects).size()!=subjects.size())throw error("Selecione de um a dez assuntos, sem repetições.");
  var unique=new LinkedHashMap<Long,QuestaoSorteio>();for(var q:candidates)if(subjects.contains(q.assuntoId()))unique.putIfAbsent(q.id(),q);var pool=List.copyOf(unique.values());
  if(subjects.stream().noneMatch(s->complete(pool.stream().filter(q->s.equals(q.assuntoId())).toList())))throw error("Pelo menos um dos assuntos selecionados deve possuir 4 questões de peso 0,5; 3 de 1,0; 2 de 1,5; e 1 de 2,0 para esta turma.");
  int[] slots={5,5,5,5,10,10,10,15,15,20},owners=new int[10];Arrays.fill(owners,-1);var ordered=new ArrayList<>(subjects);Collections.shuffle(ordered,RANDOM);
  for(int topic=0;topic<ordered.size();topic++)if(!assign(topic,ordered,pool,slots,owners,new boolean[10]))throw error("Não é possível incluir todos os assuntos selecionados nas dez questões respeitando os pesos. Revise os assuntos ou cadastre questões de outros pesos.");
  return new Plan(pool,ordered,slots,owners);
 }
 /** @param topic assunto a incluir @param subjects assuntos @param pool estoque @param slots pesos disponíveis @param owners associação parcial @param seen vagas visitadas @return se encontrou vaga compatível */
 private static boolean assign(int topic,List<Long> subjects,List<QuestaoSorteio> pool,int[] slots,int[] owners,boolean[] seen){
  /* Reorganiza reservas anteriores quando necessário, preservando as capacidades de cada peso. */
  var indexes=new ArrayList<Integer>();for(int i=0;i<slots.length;i++)indexes.add(i);Collections.shuffle(indexes,RANDOM);
  for(int slot:indexes){if(seen[slot]||pool.stream().noneMatch(q->subjects.get(topic).equals(q.assuntoId())&&weight(q)==slots[slot]))continue;seen[slot]=true;
   if(owners[slot]<0||assign(owners[slot],subjects,pool,slots,owners,seen)){owners[slot]=topic;return true;}}
  return false;
 }
 private static int weight(QuestaoSorteio q){return q.peso().multiply(BigDecimal.TEN).intValueExact();}
 private static DefaultErrorException error(String message){return new DefaultErrorException(message,HttpStatus.CONFLICT);}
}

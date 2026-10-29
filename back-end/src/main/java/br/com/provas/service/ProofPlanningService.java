package br.com.provas.service;
import br.com.provas.model.request.*;import br.com.provas.model.response.*;import br.com.provas.model.entity.QuestaoSorteio;
import br.com.provas.exceptions.DefaultErrorException;import org.springframework.http.HttpStatus;
import java.util.*;import java.math.BigDecimal;
/** Planeja quantidades por assunto/peso usando centavos inteiros e o menor déficit possível. */
public final class ProofPlanningService {
 private ProofPlanningService(){}
 private record Key(Long topic,long cents){}
 private record Group(Long topic,String name,BigDecimal weight,long units,int available,boolean required){}
 private record State(int index,int count,long value,boolean covered){}
 private record Choice(int missing,int quantity){}
 private static final Choice IMPOSSIBLE=new Choice(1000000,-1);
 /** Calcula uma distribuição exata ou informa a menor quantidade de questões a cadastrar.
  * @param config valor, quantidade e pesos permitidos por assunto
  * @param pool estoque autorizado do professor/ano/turma
  * @param names nomes dos assuntos autorizados
  * @return distribuição, disponibilidade e faltas; null em faltantes significa incompatibilidade matemática
  */
 public static PlanejamentoProvaResponse analyze(ConfiguracaoProvaRequest config,List<QuestaoSorteio> pool,Map<Long,String> names){
  /* Não soma doubles: o valor de cada peso e da prova é representado em centavos exatos. */
  validateConfiguration(config);pool=generalPool(config,pool,names);Map<Key,Integer> stock=new HashMap<>();for(var q:pool)stock.merge(new Key(q.assuntoId(),cents(q.peso())),1,Integer::sum);
  var groups=new ArrayList<Group>();long gcd=0;
  for(var subject:config.assuntos())for(var weight:subject.pesos()){long w=cents(weight);gcd=gcd(gcd,w);groups.add(new Group(subject.assuntoId(),names.getOrDefault(subject.assuntoId(),"Assunto"),weight,w,stock.getOrDefault(new Key(subject.assuntoId(),w),0),subject.exigirTodosPesos()));}
  groups.sort(Comparator.comparing(Group::topic).thenComparing(Group::weight));long target=cents(config.valorTotal());
  if(target%gcd!=0)return incompatible();final long divisor=gcd;
  groups.replaceAll(g->new Group(g.topic,g.name,g.weight,g.units/divisor,g.available,g.required));
  var solver=new Solver(groups);var first=new State(0,config.quantidadeQuestoes(),target/divisor,false);var result=solver.solve(first);
  if(result==IMPOSSIBLE)return incompatible();var rows=new ArrayList<PlanejamentoProvaResponse.Item>();var state=first;
  while(state.index<groups.size()){var choice=solver.solve(state);var group=groups.get(state.index);int n=choice.quantity;
   if(n>0)rows.add(new PlanejamentoProvaResponse.Item(group.topic,group.name,group.weight,n,group.available,Math.max(0,n-group.available)));
   state=solver.next(state,n);
  }
  return new PlanejamentoProvaResponse(result.missing==0,result.missing==0?"Distribuição disponível para liberação.":"Faltam "+result.missing+" questão(ões) para esta distribuição.",result.missing,List.copyOf(rows));
 }
 /** Confere a composição apresentada ao professor antes de efetivar a liberação.
  * @param config critérios escolhidos @param plan distribuição conferida @param pool estoque atual
  */
 public static void validatePlan(ConfiguracaoProvaRequest config,List<ComposicaoProvaRequest> plan,List<QuestaoSorteio> pool,Map<Long,String> names){
  /* A conferência é repetida na gravação: alterações no banco entre análise e liberação não passam despercebidas. */
  validateConfiguration(config);pool=generalPool(config,pool,names);if(plan==null||plan.isEmpty())throw new DefaultErrorException("Confira a distribuição antes de liberar.");
  final var availablePool=pool;
  Map<Long,Set<Long>> allowed=new HashMap<>();for(var a:config.assuntos())allowed.put(a.assuntoId(),new HashSet<>(a.pesos().stream().map(ProofPlanningService::cents).toList()));
  Set<Key> unique=new HashSet<>();Set<Long> covered=new HashSet<>();int count=0;BigDecimal value=BigDecimal.ZERO;var errors=new ArrayList<String>();
  for(var item:plan){if(item.assuntoId()==null||item.peso()==null||item.quantidade()==null||item.quantidade()<1||!allowed.getOrDefault(item.assuntoId(),Set.of()).contains(cents(item.peso()))||!unique.add(new Key(item.assuntoId(),cents(item.peso()))))throw new DefaultErrorException("A distribuição contém assunto, peso ou quantidade inválida ou repetida.");
   count+=item.quantidade();value=value.add(item.peso().multiply(BigDecimal.valueOf(item.quantidade())));covered.add(item.assuntoId());
   long available=availablePool.stream().filter(q->item.assuntoId().equals(q.assuntoId())&&item.peso().compareTo(q.peso())==0).count();if(available<item.quantidade())errors.add("Assunto '"+names.getOrDefault(item.assuntoId(),"selecionado")+"', peso "+item.peso().toPlainString().replace('.',',')+": faltam "+(item.quantidade()-available));
  }
  if(count!=config.quantidadeQuestoes()||value.compareTo(config.valorTotal())!=0||!covered.equals(allowed.keySet()))throw new DefaultErrorException("A distribuição deve incluir todos os assuntos e coincidir com o número de questões e o valor total da prova.");
  for(var subject:config.assuntos())if(subject.exigirTodosPesos())for(var weight:subject.pesos())
   if(!unique.contains(new Key(subject.assuntoId(),cents(weight))))throw new DefaultErrorException("A distribuição deve incluir pelo menos uma questão de cada peso nos assuntos com a opção ativada.");
  if(!errors.isEmpty())throw new DefaultErrorException(String.join("; ",errors)+". Confira a distribuição novamente.",HttpStatus.CONFLICT);
 }
 private static List<QuestaoSorteio> generalPool(ConfiguracaoProvaRequest config,List<QuestaoSorteio> pool,Map<Long,String> names) {
  var general=config.assuntos().stream().filter(a->"Geral".equalsIgnoreCase(names.get(a.assuntoId()))).findFirst();
  if(general.isEmpty())return pool;
  if(config.assuntos().size()!=1)throw new DefaultErrorException("Selecione Geral sozinho para considerar todos os assuntos.");
  return pool.stream().map(q->new QuestaoSorteio(q.id(),q.versaoId(),q.tipo(),q.peso(),q.enunciado(),q.alternativas(),q.respostaCorreta(),general.get().assuntoId())).toList();
 }
 private static void validateConfiguration(ConfiguracaoProvaRequest c){
  /* Cada assunto deve participar; pesos repetidos não criam capacidade adicional. */
  if(c==null||c.valorTotal()==null||c.quantidadeQuestoes()==null||c.quantidadeQuestoes()<3||c.quantidadeQuestoes()>200||c.valorTotal().signum()<=0||c.assuntos()==null||c.assuntos().isEmpty()||c.assuntos().size()>10||c.assuntos().size()>c.quantidadeQuestoes())throw new DefaultErrorException("Informe valor positivo, de 3 a 200 questões e pelo menos uma questão por assunto.");
  Set<Long> ids=new HashSet<>();for(var a:c.assuntos()){if(a==null||a.assuntoId()==null||!ids.add(a.assuntoId())||a.pesos()==null||a.pesos().isEmpty()||a.pesos().size()>20)throw new DefaultErrorException("Informe assuntos únicos com seus pesos permitidos.");Set<Long> weights=new HashSet<>();for(var w:a.pesos())if(w==null||w.signum()<=0||!weights.add(cents(w)))throw new DefaultErrorException("Informe pesos positivos e sem repetições por assunto.");}
 }
 private static long cents(BigDecimal value){try{return value.movePointRight(2).longValueExact();}catch(ArithmeticException e){throw new DefaultErrorException("Use valores com até duas casas decimais.");}}
 private static long gcd(long a,long b){while(b!=0){long n=a%b;a=b;b=n;}return a;}
 private static PlanejamentoProvaResponse incompatible(){return new PlanejamentoProvaResponse(false,"Não existe combinação com estes assuntos e pesos que atinja simultaneamente o valor total e a quantidade de questões. Ajuste os critérios.",null,List.of());}
 private static final class Solver {
  private final List<Group> groups;private final Map<State,Choice> memo=new HashMap<>();private final long[] minimum,maximum,gcd;private final int[] topics,stockRemaining;
  private final int[] requiredCount;private final long[] requiredValue;
  Solver(List<Group> groups){this.groups=groups;int n=groups.size();minimum=new long[n+1];maximum=new long[n+1];gcd=new long[n+1];topics=new int[n+1];stockRemaining=new int[n+1];minimum[n]=Long.MAX_VALUE;
   requiredCount=new int[n+1];requiredValue=new long[n+1];
   for(int i=n-1;i>=0;i--){long w=groups.get(i).units;requiredCount[i]=requiredCount[i+1]+(groups.get(i).required?1:0);requiredValue[i]=requiredValue[i+1]+(groups.get(i).required?w:0);stockRemaining[i]=Math.min(200,stockRemaining[i+1]+groups.get(i).available);minimum[i]=Math.min(w,minimum[i+1]);maximum[i]=Math.max(w,maximum[i+1]);gcd[i]=ProofPlanningService.gcd(w,gcd[i+1]);topics[i]=topics[i+1]+(i==n-1||!groups.get(i).topic.equals(groups.get(i+1).topic)?1:0);}}
  State next(State s,int n){var g=groups.get(s.index);boolean same=s.index+1<groups.size()&&g.topic.equals(groups.get(s.index+1).topic);return new State(s.index+1,s.count-n,s.value-n*g.units,same&&(s.covered||n>0));}
  Choice solve(State s){
   /* Memorização elimina combinações repetidas; limites e MDC descartam somas impossíveis cedo. */
   if(s.index==groups.size())return s.count==0&&s.value==0?new Choice(0,0):IMPOSSIBLE;
   if(s.count<requiredCount[s.index]||s.value<requiredValue[s.index]||s.count<topics[s.index]-(s.covered?1:0)||s.value<s.count*minimum[s.index]||s.value>s.count*maximum[s.index]||s.value%gcd[s.index]!=0)return IMPOSSIBLE;
   var cached=memo.get(s);if(cached!=null)return cached;if(memo.size()>250000)throw new DefaultErrorException("Esta configuração tem muitas combinações. Reduza a variedade de pesos por assunto para concluir a análise.");
   var group=groups.get(s.index);int max=(int)Math.min(s.count,s.value/group.units);var choices=new ArrayList<Integer>();for(int n=group.required?1:0;n<=max;n++)choices.add(n);
   choices.sort(Comparator.<Integer>comparingInt(n->Math.max(0,n-group.available)).thenComparing(Comparator.reverseOrder()));Choice best=IMPOSSIBLE;int lowerBound=Math.max(0,s.count-stockRemaining[s.index]);
   boolean last=s.index+1==groups.size()||!group.topic.equals(groups.get(s.index+1).topic);
   for(int n:choices){if(last&&!s.covered&&n==0)continue;int missing=Math.max(0,n-group.available);if(missing>=best.missing)continue;var child=solve(next(s,n));if(child!=IMPOSSIBLE&&missing+child.missing<best.missing){best=new Choice(missing+child.missing,n);if(best.missing==lowerBound)break;}}
   memo.put(s,best);return best;
  }
 }
}

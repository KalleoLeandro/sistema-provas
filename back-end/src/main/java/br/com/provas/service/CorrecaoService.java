package br.com.provas.service;
import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.request.CorrigirQuestaoRequest;
import br.com.provas.model.request.SalvarCorrecaoProvaRequest;
import br.com.provas.model.response.*;
import br.com.provas.repository.CorrecaoRepository;
import java.time.Clock;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** Corrige entregas do próprio professor sem alterar o conteúdo ou as respostas do aluno. */
@Service
public class CorrecaoService {
 private final CorrecaoRepository repository;private final Clock clock;
 public CorrecaoService(CorrecaoRepository repository,Clock clock){this.repository=repository;this.clock=clock;}
 /**
  * Lista entregas autorizadas com nota parcial e quantidade de discursivas pendentes.
  * @param professor autor autenticado
  * @param turma filtro opcional da turma original da liberação
  * @param situacao todas, pendentes ou corrigidas
  * @param pagina página iniciando em um
  * @param tamanho quantidade de 15, 25 ou 50
  * @return página de entregas, sem incluir provas em andamento
  */
 @Transactional(readOnly=true)
 public PaginaResponse<ProvaCorrecaoResumoResponse> findAll(Long professor,Long turma,String situacao,int pagina,int tamanho){return findAll(professor,turma,situacao,pagina,tamanho,new br.com.provas.model.request.OrdenacaoRequest(null,null));}
 /** Consulta a lista inteira na ordem escolhida antes de aplicar a página solicitada.
  * @param order coluna e direção validadas; preserva a ordem padrão quando ausente
  * @return página no mesmo escopo autorizado, sem modificar os filtros
  */
 @Transactional(readOnly=true)
 public PaginaResponse<ProvaCorrecaoResumoResponse> findAll(Long professor,Long turma,String situacao,int pagina,int tamanho,br.com.provas.model.request.OrdenacaoRequest order){
 /* A coluna é resolvida por uma lista permitida no repository, sem interpolar entrada livre no SQL. */
 
  /* Autoria integra todas as consultas; a turma informada nunca amplia acesso a outro professor. */
  if(pagina<1||!Set.of(15,25,50).contains(tamanho)||!Set.of("todas","pendentes","corrigidas").contains(situacao))throw new DefaultErrorException("Informe filtros e paginação válidos.");
  long total=repository.count(professor,turma,situacao);int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
  return new PaginaResponse<>((order.ordenacao()==null?repository.findAll(professor,turma,situacao,tamanho,(long)(atual-1)*tamanho):repository.findAll(professor,turma,situacao,tamanho,(long)(atual-1)*tamanho,order)),total,atual,tamanho);
 }
 /**
  * Carrega a cópia entregue e as respostas originais para correção.
  * @param professor autor autenticado
  * @param id tentativa entregue
  * @return prova e questões; dados restritos ao professor, sem publicação para o aluno
  */
 @Transactional(readOnly=true)
 public ProvaCorrecaoResponse get(Long professor,Long id){
  /* O professor acessa somente suas entregas, inclusive históricas após troca da turma do aluno. */
  var prova=repository.findById(professor,id).orElseThrow(()->new DefaultErrorException("Prova entregue não encontrada.",HttpStatus.NOT_FOUND));
  return new ProvaCorrecaoResponse(prova,repository.questions(professor,id));
 }
 /**
  * Salva a correção completa em uma única transação, sem gravação parcial.
  * @param professor autor autenticado
  * @param id tentativa entregue
  * @param request todas as pontuações, feedbacks e revisões conhecidas
  * @return prova corrigida com total atualizado
  */
 @Transactional
 public ProvaCorrecaoResponse save(Long professor,Long id,SalvarCorrecaoProvaRequest request){
  /* Correções concluídas continuam revisáveis pelo autor, respeitando os limites e a revisão otimista. */
  var prova=get(professor,id);
  /* Exige exatamente as questões da cópia entregue, sem omissões ou números repetidos. */
  var itens=new java.util.HashMap<Integer,CorrigirQuestaoRequest>();
  for(var item:request.questoes())if(itens.putIfAbsent(item.numero(),item.correcao())!=null)throw new DefaultErrorException("Informe cada questão uma única vez.");
  if(itens.size()!=prova.questoes().size()||!prova.questoes().stream().allMatch(q->itens.containsKey(q.numero())))throw new DefaultErrorException("Envie a correção de todas as questões da prova.");
  /* Confere todos os campos e revisões antes de iniciar a gravação. */
  for(var q:prova.questoes()){
   var correcao=itens.get(q.numero());validate(q,correcao);
   if(q.revisao()!=correcao.revisao())throw conflict();
  }
  /* Cada escrita mantém comparação de revisão; conflito posterior desfaz toda a transação. */
  var agora=clock.instant();
  for(var q:prova.questoes())if(!repository.update(professor,id,q.numero(),itens.get(q.numero()),agora))throw conflict();
  return get(professor,id);
 }
 /** @param q questão entregue @param request nota e feedback propostos */
 private void validate(QuestaoCorrecaoResponse q,CorrigirQuestaoRequest request){
  /* O peso limita a nota; branco permanece zero e objetiva conserva a correção automática. */
  if(request.pontuacao()==null||request.pontuacao().signum()<0||request.pontuacao().compareTo(q.peso())>0)throw new DefaultErrorException("Questão "+q.numero()+": informe pontuação entre zero e o peso da questão.");
  boolean emBranco=q.resposta()==null||q.resposta().isBlank();
  if(emBranco&&request.pontuacao().compareTo(BigDecimal.ZERO)!=0)throw new DefaultErrorException("Questão "+q.numero()+": questões não respondidas devem permanecer com zero.");
  if("objetiva".equals(q.tipo())&&q.pontuacao()!=null&&request.pontuacao().compareTo(q.pontuacao())!=0)throw new DefaultErrorException("Questão "+q.numero()+": a pontuação objetiva é calculada automaticamente.");
 }
 /** @return conflito padronizado que preserva a correção existente */
 private DefaultErrorException conflict(){return new DefaultErrorException("A correção foi alterada em outra sessão. Atualize a prova antes de salvar.",HttpStatus.CONFLICT);}
}

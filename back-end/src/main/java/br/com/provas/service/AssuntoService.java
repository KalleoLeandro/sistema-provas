package br.com.provas.service;
import br.com.provas.repository.AssuntoRepository;
import br.com.provas.model.request.AssuntoRequest;
import br.com.provas.model.response.AssuntoResponse;
import br.com.provas.exceptions.DefaultErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class AssuntoService {
 private final AssuntoRepository repository;
 public AssuntoService(AssuntoRepository repository){this.repository=repository;}
 /** Busca sugestões na matéria ministrada pelo professor, ignorando caixa e acentos no banco.
  * @param professor identidade da sessão
  * @param materia matéria selecionada
  * @param busca texto parcial, vazio para o catálogo da matéria
  * @return sugestões ordenadas
  */
 @Transactional(readOnly=true)
 public List<AssuntoResponse> search(Long professor,Long materia,String busca){
  /* O catálogo é compartilhado por matéria, mas somente professores com atribuição ativa podem consultá-lo. */
  authorize(professor,materia);return repository.search(materia,busca.strip());
 }
 /** Cria ou reutiliza um assunto equivalente sem duplicar nomes na mesma matéria.
  * @param professor identidade da sessão
  * @param request matéria e nome validados
  * @return assunto que pode ser selecionado como chip
  */
 @Transactional
 public AssuntoResponse create(Long professor,AssuntoRequest request){
  /* A chave única da matéria/nome protege criações simultâneas; nomes repetidos retornam o assunto existente. */
  if ("Geral".equalsIgnoreCase(request.nome())) throw new DefaultErrorException("Geral deve ser usado apenas na libera\u00e7\u00e3o de provas.");
  authorize(professor,request.materiaId());return repository.save(request.materiaId(),request.nome());
 }
 /** @param professor identidade autenticada @param materia contexto autorizado */
 private void authorize(Long professor,Long materia){
  /* Não aceita catalogar assuntos de matérias fora das atribuições do professor. */
  if(!repository.allowed(professor,materia))throw new DefaultErrorException("Você não possui vínculo ativo com esta matéria.",HttpStatus.FORBIDDEN);
 }
}

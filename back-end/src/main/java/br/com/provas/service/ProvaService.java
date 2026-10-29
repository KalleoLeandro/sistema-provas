package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.LiberacaoProva;
import br.com.provas.model.request.LiberarProvaRequest;
import br.com.provas.model.response.*;
import br.com.provas.repository.ProvaRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Controla a liberação por turma de provas e seus períodos de disponibilidade. */
@Service
public class ProvaService {
    private final ProvaRepository repository;
    private final Clock clock;
    private final br.com.provas.security.TokenCipher tokenCipher;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    public ProvaService(ProvaRepository repository,Clock clock,org.springframework.security.crypto.password.PasswordEncoder encoder,br.com.provas.security.TokenCipher tokenCipher) { this.repository=repository;this.clock=clock;this.encoder=encoder;this.tokenCipher=tokenCipher; }

    /** @param professorId identidade da sessão @param turmaId turma consultada @param pagina página iniciando em 1 @param tamanho limite de 15, 25 ou 50 @return liberações exclusivamente deste professor */
    @Transactional(readOnly=true)
    public PaginaResponse<LiberacaoProvaResponse> findAll(Long professorId,Long turmaId,int pagina,int tamanho) {return findAll(professorId,turmaId,pagina,tamanho,new br.com.provas.model.request.OrdenacaoRequest(null,null));}
 /** Consulta a lista inteira na ordem escolhida antes de aplicar a página solicitada.
  * @param order coluna e direção validadas; preserva a ordem padrão quando ausente
  * @return página no mesmo escopo autorizado, sem modificar os filtros
  */
 @Transactional(readOnly=true)
 public PaginaResponse<LiberacaoProvaResponse> findAll(Long professorId,Long turmaId,int pagina,int tamanho,br.com.provas.model.request.OrdenacaoRequest order) {
 /* A coluna é resolvida por uma lista permitida no repository, sem interpolar entrada livre no SQL. */
 
        /* O vínculo atual é obrigatório; IDs de alunos de outras turmas não revelam informações. */
        if(!repository.hasClass(professorId,turmaId)) throw new DefaultErrorException("Turma não encontrada nos seus vínculos.",HttpStatus.NOT_FOUND);
        if(pagina<1 || !Set.of(15,25,50).contains(tamanho)) throw new DefaultErrorException("Informe página positiva e 15, 25 ou 50 itens por página.");
        /* As duas consultas filtram a autoria. A situação é calculada com um mesmo instante do servidor. */
        long total=repository.count(professorId,turmaId);
        int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
        Instant now=clock.instant();
        var itens=(order.ordenacao()==null?repository.findAll(professorId,turmaId,tamanho,(long)(atual-1)*tamanho):repository.findAll(professorId,turmaId,tamanho,(long)(atual-1)*tamanho,order,now)).stream().map(l->toResponse(l,now)).toList();
        return new PaginaResponse<>(itens,total,atual,tamanho);
    }

    /** @param professorId identidade da sessão @param turmaId turma destinatária @param request matéria, título e período @return liberação persistida e situação atual */
    @Transactional
    public LiberacaoProvaResponse release(Long professorId,Long turmaId,LiberarProvaRequest request) {
        /* O servidor valida o período independentemente do formulário e usa instantes UTC. */
        Instant now=clock.instant();
        if(request.disponivelEm()==null || request.encerraEm()==null || !request.encerraEm().isAfter(request.disponivelEm()) || !request.encerraEm().isAfter(now))
            throw new DefaultErrorException("Informe um encerramento futuro e posterior ao início da disponibilidade.");
        /* A liberação vale para a turma inteira; o vínculo ativo de matéria é bloqueado durante a gravação. */
        if(!repository.lockClassSubject(professorId,turmaId,request.materiaId())) throw new DefaultErrorException("Selecione uma turma e uma matéria em que você possui vínculo ativo.",HttpStatus.FORBIDDEN);
        /* Esta etapa registra a autorização e seu prazo; a composição e a realização da prova pertencem ao módulo de execução. */
        if(request.token()==null)throw new DefaultErrorException("Informe um token de início de 4 a 64 caracteres.");
        /* A liberação só é gravada se cada assunto puder aparecer sem alterar a distribuição conferida. */
        validateSubjects(professorId,turmaId,request);
        var saved=repository.save(professorId,turmaId,request,encoder.encode(request.token()),tokenCipher.encrypt(request.token()));
        repository.saveSubjects(saved.id(),request.assuntos());return toResponse(saved,now);
    }

    /**
     * Edita a configuração da turma e permite trocar o token sem expor o anterior.
     * @param professorId autor autenticado
     * @param turmaId turma vinculada
     * @param id liberação a alterar
     * @param request nova configuração; token ausente mantém o atual
     */
    @Transactional
    public void update(Long professorId,Long turmaId,Long id,LiberarProvaRequest request){
        /* Alterações afetam novos inícios; cópias e prazos de tentativas iniciadas são preservados. */
        if(!request.encerraEm().isAfter(clock.instant())||!request.encerraEm().isAfter(request.disponivelEm()))throw new DefaultErrorException("Informe um período válido com encerramento futuro.");
        if(!repository.lockClassSubject(professorId,turmaId,request.materiaId()))throw new DefaultErrorException("Vínculo não autorizado.",HttpStatus.FORBIDDEN);
        /* O token legado não pode ser recuperado do hash: solicita substituição explícita na primeira edição. */
        var current=repository.findById(professorId,turmaId,id).orElseThrow(()->new DefaultErrorException("Liberação não encontrada.",HttpStatus.NOT_FOUND));
        if(current.tokenCifrado()==null&&request.token()==null)throw new DefaultErrorException("Defina um novo token para esta liberação.");
        validateSubjects(professorId,turmaId,request);
        /* Hash e cópia cifrada são substituídos juntos; em branco, ambos permanecem coerentes. */
        if(!repository.update(professorId,turmaId,id,request,request.token()==null?null:encoder.encode(request.token()),request.token()==null?null:tokenCipher.encrypt(request.token())))throw new DefaultErrorException("Liberação não encontrada.",HttpStatus.NOT_FOUND);
        repository.saveSubjects(id,request.assuntos());
    }

    /**
     * Carrega os dados atuais para edição, incluindo o token apenas para o autor autenticado.
     * @param professorId autor da liberação
     * @param turmaId turma autorizada
     * @param id liberação
     * @return título, matéria, prazo, duração e token consultável
     */
    @Transactional(readOnly=true)
    public LiberacaoDetalheResponse getForEdit(Long professorId,Long turmaId,Long id){
        /* Filtrar autor e turma na própria consulta impede revelar tokens de outro professor. */
        if(!repository.hasClass(professorId,turmaId))throw new DefaultErrorException("Turma não autorizada.",HttpStatus.FORBIDDEN);
        var l=repository.findById(professorId,turmaId,id).orElseThrow(()->new DefaultErrorException("Liberação não encontrada.",HttpStatus.NOT_FOUND));
        /* A chave privada local recupera somente tokens novos; hashes legados exigem definição de um novo token. */
        String token=l.tokenCifrado()==null?null:tokenCipher.decrypt(l.tokenCifrado());
        return new LiberacaoDetalheResponse(l.id(),l.titulo(),l.materiaId(),l.materia(),l.disponivelEm(),l.encerraEm(),l.duracaoMinutos(),token,token==null,repository.subjects(l.id()),l.configuracao(),l.composicao(),l.valorTotal(),l.quantidadeQuestoes());
    }

    /**
     * Consulta as liberações da turma atual do aluno, mantendo a autoria do professor.
     * @param professorId identidade da sessão
     * @param alunoId aluno das turmas do professor
     * @param pagina página solicitada
     * @param tamanho limite de itens
     * @return liberações da turma, sem criar registros individuais
     */
    @Transactional(readOnly=true)
    public PaginaResponse<LiberacaoProvaResponse> findByStudent(Long professorId,Long alunoId,int pagina,int tamanho) {return findByStudent(professorId,alunoId,pagina,tamanho,new br.com.provas.model.request.OrdenacaoRequest(null,null));}
 /** Consulta a lista inteira na ordem escolhida antes de aplicar a página solicitada.
  * @param order coluna e direção validadas; preserva a ordem padrão quando ausente
  * @return página no mesmo escopo autorizado, sem modificar os filtros
  */
 @Transactional(readOnly=true)
 public PaginaResponse<LiberacaoProvaResponse> findByStudent(Long professorId,Long alunoId,int pagina,int tamanho,br.com.provas.model.request.OrdenacaoRequest order) {
 /* A coluna é resolvida por uma lista permitida no repository, sem interpolar entrada livre no SQL. */
 
        /* Resolver a turma no servidor impede ampliar acesso informando outro aluno. */
        Long turmaId=repository.findStudentClass(professorId,alunoId);
        if(turmaId==null) throw new DefaultErrorException("Aluno não encontrado nas suas turmas.",HttpStatus.NOT_FOUND);
        return findAll(professorId,turmaId,pagina,tamanho,order);
    }

    /** Consulta o estoque atual por assunto e peso sem impor pesos ou mínimos fixos.
     * @param professor identidade autenticada @param turma destino @param materia matéria
     * @return assuntos e quantidades disponíveis de cada peso
     */
    @Transactional
    public java.util.List<AssuntoProvaResponse> subjectOptions(Long professor,Long turma,Long materia){
        /* O catálogo nunca inclui contagens de questões de outro professor ou outra turma. */
        if(!repository.hasSubjectAssignment(professor,turma,materia))throw new DefaultErrorException("Vínculo não autorizado.",HttpStatus.FORBIDDEN);
        repository.ensureGeneralSubject(materia);
        var pool=repository.subjectCandidates(professor,turma,materia);
        return repository.subjectOptions(professor,turma,materia).stream().map(a->{
            var quantities=pool.stream().filter(q->"Geral".equalsIgnoreCase(a.nome())||a.id().equals(q.assuntoId())).map(q->q.peso().stripTrailingZeros()).distinct().sorted().map(weight->new QuantidadeQuestoesResponse(weight,0,(int)pool.stream().filter(q->("Geral".equalsIgnoreCase(a.nome())||a.id().equals(q.assuntoId()))&&weight.compareTo(q.peso())==0).count(),0)).toList();
            return new AssuntoProvaResponse(a.id(),a.materiaId(),a.nome(),quantities,false);
        }).toList();
    }
    /** Calcula a distribuição antes de gravar uma liberação.
     * @param professor autor @param turma destino @param request valor, número e pesos permitidos por assunto
     * @return combinação disponível ou quantidade mínima de questões faltantes
     */
    @Transactional(readOnly=true)
    public PlanejamentoProvaResponse analyze(Long professor,Long turma,br.com.provas.model.request.PlanejarProvaRequest request){
        /* A prévia é somente leitura; a liberação repete a conferência de estoque na gravação. */
        var names=subjectNames(professor,turma,request.materiaId(),request.configuracao());
        return ProofPlanningService.analyze(request.configuracao(),repository.subjectCandidates(professor,turma,request.materiaId()),names);
    }
    /** @param professor autor @param turma destino @param request critérios e distribuição conferida */
    private void validateSubjects(Long professor,Long turma,LiberarProvaRequest request){
        /* Quantidades são escolhidas automaticamente na análise, mas a composição apresentada é
         * preservada na liberação; não é trocada silenciosamente por outra distribuição. */
        var names=subjectNames(professor,turma,request.materiaId(),request.configuracao());
        ProofPlanningService.validatePlan(request.configuracao(),request.composicao(),repository.subjectCandidates(professor,turma,request.materiaId()),names);
    }
    /** @param professor autor @param turma destino @param materia contexto @param config assuntos escolhidos @return catálogo autorizado */
    private java.util.Map<Long,String> subjectNames(Long professor,Long turma,Long materia,br.com.provas.model.request.ConfiguracaoProvaRequest config){
        /* IDs da tela não ampliam o vínculo do professor. */
        if(!repository.hasSubjectAssignment(professor,turma,materia))throw new DefaultErrorException("Vínculo não autorizado.",HttpStatus.FORBIDDEN);
        if(config==null||config.assuntos()==null)throw new DefaultErrorException("Informe os assuntos e os pesos permitidos.");
        var names=repository.subjectOptions(professor,turma,materia).stream().collect(java.util.stream.Collectors.toMap(AssuntoResponse::id,AssuntoResponse::nome));
        if(config.assuntos().stream().anyMatch(a->!names.containsKey(a.assuntoId())))throw new DefaultErrorException("Selecione apenas assuntos da matéria escolhida.");return names;
    }
    /** @param liberacao registro persistido @param now instante único da consulta @return situação derivada, sem depender de atualização agendada no banco */
    private LiberacaoProvaResponse toResponse(LiberacaoProva liberacao,Instant now) {
        /* O intervalo inclui o início e exclui o encerramento: no instante final o prazo já está encerrado. */
        String situacao=now.isBefore(liberacao.disponivelEm())?"agendada":!now.isBefore(liberacao.encerraEm())?"encerrada":"disponivel";
        return new LiberacaoProvaResponse(liberacao.id(),liberacao.titulo(),liberacao.materiaId(),liberacao.materia(),liberacao.disponivelEm(),liberacao.encerraEm(),situacao,liberacao.duracaoMinutos(),liberacao.tokenConfigurado(),repository.subjects(liberacao.id()),liberacao.valorTotal(),liberacao.quantidadeQuestoes());
    }
}

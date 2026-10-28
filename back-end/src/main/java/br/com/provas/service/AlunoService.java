package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.response.AlunoResponse;
import br.com.provas.model.response.PaginaResponse;
import br.com.provas.repository.AlunoRepository;
import java.util.Set;
import br.com.provas.model.request.AlunoRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gerencia o cadastro administrativo e limita a consulta do professor às suas turmas ativas. */
@Service
public class AlunoService {
    private final AlunoRepository repository;
    public AlunoService(AlunoRepository repository) { this.repository=repository; }
    /**
     * Pesquisa alunos no escopo autorizado, contando e paginando somente os resultados filtrados.
     * @param professorId professor da sessão ou null para consulta administrativa
     * @param filtro nome/CGM, status, turma e ordenação
     * @param pagina página iniciando em um
     * @param tamanho limite permitido
     * @return página filtrada
     */
    @Transactional(readOnly=true)
    public PaginaResponse<AlunoResponse> search(Long professorId,br.com.provas.model.request.AlunoFiltroRequest filtro,int pagina,int tamanho){
        /* A pesquisa é aplicada no banco inteiro, sem filtrar apenas os alunos da página atual.
         * O escopo do professor permanece obrigatório tanto na contagem quanto na busca.
         * A ordenação é aplicada a todos os registros filtrados antes do LIMIT/OFFSET. */
        if(pagina<1||!Set.of(15,25,50).contains(tamanho))throw new DefaultErrorException("Informe página positiva e 15, 25 ou 50 itens por página.");
        long total=repository.countFiltered(professorId,filtro);
        int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
        return new PaginaResponse<>(repository.findFiltered(professorId,filtro,tamanho,(long)(atual-1)*tamanho),total,atual,tamanho);
    }
    /**
     * Consulta o aluno para edição administrativa.
     * @param id identificador do aluno
     * @return dados atuais, sem informações de provas ou credenciais
     */
    @Transactional(readOnly=true)
    public AlunoResponse findById(Long id){
        /* O endpoint exige administrador; recursos ausentes têm resposta padronizada. */
        return repository.findById(id).orElseThrow(()->new DefaultErrorException("Aluno não encontrado.",HttpStatus.NOT_FOUND));
    }
    /**
     * Atualiza o cadastro escolar preservando a identidade e o histórico de provas.
     * @param id identificador existente
     * @param request nome, número, CGM, status e turma validados
     * @return cadastro atualizado
     */
    @Transactional
    public AlunoResponse update(Long id,AlunoRequest request){
        /* Serializa alterações no cadastro sem recriar o aluno ou tocar suas respostas e tentativas. */
        if(!repository.lockById(id))throw new DefaultErrorException("Aluno não encontrado.",HttpStatus.NOT_FOUND);
        if(!repository.turmaExists(request.turmaId()))throw new DefaultErrorException("turmaId: Selecione uma turma cadastrada.");
        /* O CGM continua único. Transferência e troca de CGM passam a valer na próxima validação de sessão. */
        try{repository.update(id,request);}
        catch(DuplicateKeyException e){throw new DefaultErrorException("Já existe um aluno cadastrado com este CGM.",HttpStatus.CONFLICT);}
        return findById(id);
    }

    /**
     * Retorna uma página de alunos sem duplicar quem pertence a uma turma com várias matérias do professor.
     * @param professorId identidade obtida da sessão
     * @param turmaId filtro opcional, sempre combinado com a autorização do professor
     * @param pagina página solicitada, iniciando em um
     * @param tamanho quantidade permitida: 15, 25 ou 50
     * @return página e total da consulta autorizada
     */
    @Transactional(readOnly=true)
    public PaginaResponse<AlunoResponse> findAll(Long professorId, Long turmaId, int pagina, int tamanho) {
        /* A paginação é limitada também na regra de negócio, sem aceitar tamanhos arbitrários. */
        if (pagina<1 || !Set.of(15,25,50).contains(tamanho)) throw new DefaultErrorException("Informe página positiva e 15, 25 ou 50 itens por página.");
        /* O filtro por professor pertence às duas consultas; informar outra turma nunca amplia a permissão.
         * Quando uma página deixa de existir, retorna a última disponível sem perder o filtro. */
        long total=repository.countByProfessor(professorId,turmaId);
        int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
        return new PaginaResponse<>(repository.findByProfessor(professorId,turmaId,tamanho,(long)(atual-1)*tamanho),total,atual,tamanho);
    }
    /**
     * Cadastra um aluno, operação exposta somente ao administrador pelo controller e security config.
     * @param request nome, número opcional, CGM, situação escolar e turma
     * @return cadastro confirmado pelo banco
     */
    @Transactional
    public AlunoResponse create(AlunoRequest request) {
        /* A turma precisa existir e fica protegida de remoção durante a gravação. */
        if(!repository.turmaExists(request.turmaId())) throw new DefaultErrorException("turmaId: Selecione uma turma cadastrada.");
        /* CGM é uma identificação única e textual, preservando zeros iniciais. Número não é obrigatório.
         * M/T representa situação escolar, sem criar credenciais ou liberar login de aluno nesta etapa. */
        try { return repository.save(request); }
        catch(DuplicateKeyException exception) { throw new DefaultErrorException("Já existe um aluno cadastrado com este CGM.",HttpStatus.CONFLICT); }
    }

    /** @param pagina página iniciando em 1 @param tamanho limite de 15, 25 ou 50 @return consulta administrativa de todos os alunos */
    @Transactional(readOnly=true)
    public PaginaResponse<AlunoResponse> findAllForAdmin(int pagina,int tamanho) {
        /* A autorização administrativa é aplicada no endpoint; a paginação mantém os mesmos limites da consulta do professor. */
        if(pagina<1 || !Set.of(15,25,50).contains(tamanho)) throw new DefaultErrorException("Informe página positiva e 15, 25 ou 50 itens por página.");
        long total=repository.countAll();
        int atual=(int)Math.min(pagina,Math.max(1,(total+tamanho-1)/tamanho));
        return new PaginaResponse<>(repository.findAll(tamanho,(long)(atual-1)*tamanho),total,atual,tamanho);
    }

}

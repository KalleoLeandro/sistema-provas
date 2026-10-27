package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.request.TurmaRequest;
import br.com.provas.model.response.TurmaResponse;
import br.com.provas.repository.TurmaRepository;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Mantém turmas identificadas pela combinação de ensino, ano e letra. */
@Service
public class TurmaService {
    private final TurmaRepository repository;

    public TurmaService(TurmaRepository repository) { this.repository = repository; }

    /** Consulta uma turma existente para edição administrativa.
     * @param id identificador da turma
     * @return identificação atual
     */
    public TurmaResponse findById(Long id){
        /* Recursos ausentes seguem o mesmo contrato das demais telas de edição. */
        var turma=repository.findById(id,false).orElseThrow(()->new DefaultErrorException("Turma não encontrada.",HttpStatus.NOT_FOUND));
        return new TurmaResponse(turma.id(),turma.ensino(),turma.ano(),turma.letra());
    }
    /** Atualiza a identificação sem recriar a turma ou seus vínculos.
     * @param id identificador existente
     * @param request ensino, ano e letra validados
     * @return turma atualizada
     */
    @org.springframework.transaction.annotation.Transactional
    public TurmaResponse update(Long id,TurmaRequest request){
        /* A identidade permanece estável e a chave única impede identificações duplicadas,
         * inclusive quando duas edições são enviadas ao mesmo tempo. */
        if(repository.findById(id,true).isEmpty())throw new DefaultErrorException("Turma não encontrada.",HttpStatus.NOT_FOUND);
        try{repository.update(id,request);}catch(DuplicateKeyException e){throw new DefaultErrorException("Já existe uma turma com este ensino, ano e letra.",HttpStatus.CONFLICT);}
        return findById(id);
    }
    /** Exclui somente turmas sem pessoas vinculadas, preservando referências históricas.
     * @param id identificador da turma a remover
     */
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id){
        /* O bloqueio da turma e as chaves estrangeiras protegem contra novos vínculos
         * concorrentes. Alunos em qualquer status e professores inativos também impedem exclusão. */
        if(repository.findById(id,true).isEmpty())throw new DefaultErrorException("Turma não encontrada.",HttpStatus.NOT_FOUND);
        if(repository.hasPeople(id))throw new DefaultErrorException("Não é possível excluir uma turma com alunos ou professores vinculados.",HttpStatus.CONFLICT);
        /* Questões e liberações ainda referenciadas não são apagadas em cascata. */
        try{repository.delete(id);}catch(org.springframework.dao.DataIntegrityViolationException e){throw new DefaultErrorException("Não é possível excluir esta turma: existem questões ou provas vinculadas.",HttpStatus.CONFLICT);}
    }
    /**
     * Cadastra uma turma com os dados validados pelo request.
     *
     * @param request ensino, ano e letra da turma
     * @return turma cadastrada com seu identificador
     * @throws DefaultErrorException quando a mesma identificação já está cadastrada
     */
    public TurmaResponse create(TurmaRequest request) {
        /*
         * A turma é única dentro da combinação ensino, ano e letra. A restrição
         * do banco também protege duas solicitações simultâneas de cadastro.
         * Fundamental e médio podem compartilhar ano e letra por serem ensinos distintos.
         */
        try {
            var turma = repository.save(request.ensino(), request.ano(), request.letra());
            return new TurmaResponse(turma.id(), turma.ensino(), turma.ano(), turma.letra());
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Já existe uma turma com este ensino, ano e letra.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Lista as turmas para conferência dos cadastros realizados.
     *
     * @return turmas ordenadas por ensino, ano e letra, ou uma lista vazia
     */
    public List<TurmaResponse> findAll() {
        /*
         * Preserva a ordem da consulta para agrupar o mesmo ensino e apresentar
         * os anos e as letras em sequência. Nenhuma turma cadastrada não é erro.
         */
        return repository.findAll().stream().map(turma ->
            new TurmaResponse(turma.id(), turma.ensino(), turma.ano(), turma.letra())).toList();
    }
}

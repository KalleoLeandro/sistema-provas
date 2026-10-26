package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.request.MateriaRequest;
import br.com.provas.model.response.MateriaResponse;
import br.com.provas.repository.MateriaRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Mantém o cadastro de matérias, identificado por um nome obrigatório e único.
 * Traduz ausência de registros e restrições do banco em erros de negócio que
 * possam ser apresentados ao usuário sem expor detalhes da persistência.
 */
@Service
public class MateriaService {
    private final MateriaRepository repository;

    public MateriaService(MateriaRepository repository) { this.repository = repository; }

    /**
     * Lista as matérias cadastradas.
     *
     * @return matérias na ordem fornecida pelo repository, ou lista vazia
     */
    public List<MateriaResponse> findAll() {
        /*
         * Preserva a ordenação alfabética da consulta e retorna apenas os dados
         * do cadastro; ausência de matérias é uma lista vazia, não um erro.
         */
        return repository.findAll().stream()
            .map(materia -> new MateriaResponse(materia.id(), materia.nome())).toList();
    }

    /**
     * Consulta uma matéria pelo identificador.
     *
     * @param id identificador da matéria
     * @return dados da matéria encontrada
     * @throws DefaultErrorException quando a matéria não existe
     */
    public MateriaResponse findById(Long id) {
        /*
         * Exige um registro existente antes de preencher o formulário de edição.
         * Um ID removido ou desconhecido é informado como recurso não encontrado.
         */
        var materia = repository.findById(id).orElseThrow(() -> new DefaultErrorException("Matéria não encontrada.", HttpStatus.NOT_FOUND));
        return new MateriaResponse(materia.id(), materia.nome());
    }

    /**
     * Atualiza o nome de uma matéria existente.
     *
     * @param id identificador da matéria
     * @param request novo nome da matéria
     * @return dados atualizados
     * @throws DefaultErrorException quando o nome é inválido, repetido ou o registro não existe
     */
    public MateriaResponse update(Long id, MateriaRequest request) {
        String nome = validateName(request);
        /*
         * O ID é preservado e somente o nome é alterado. A quantidade de registros
         * afetados detecta ausência do cadastro; a restrição de unicidade do banco
         * impede nomes repetidos e transforma esse caso em conflito de negócio.
         */
        try {
            if (repository.update(id, nome) == 0) throw new DefaultErrorException("Matéria não encontrada.", HttpStatus.NOT_FOUND);
            return new MateriaResponse(id, nome);
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Esta matéria já está cadastrada.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Exclui uma matéria quando o banco permite a remoção.
     *
     * @param id identificador da matéria
     * @throws DefaultErrorException quando a matéria não existe ou sua exclusão viola a integridade do cadastro
     */
    public void delete(Long id) {
        /*
         * A remoção é definitiva, mas não pode romper vínculos protegidos pelo banco.
         * Um bloqueio de integridade é apresentado como conflito; nenhum registro
         * afetado indica que a matéria já foi removida ou nunca existiu.
         */
        try {
            if (repository.delete(id) == 0) throw new DefaultErrorException("Matéria não encontrada.", HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException exception) {
            throw new DefaultErrorException("Esta matéria está vinculada a outros registros e não pode ser excluída.", HttpStatus.CONFLICT);
        }
    }

    /**
     * Valida e normaliza o nome usado na edição.
     *
     * @param request dados recebidos para atualização
     * @return nome sem espaços externos
     * @throws DefaultErrorException quando o nome é ausente, vazio ou excede o limite
     */
    private String validateName(MateriaRequest request) {
        /*
         * Mantém a exigência de nome preenchido e de até 120 caracteres também
         * para chamadas internas, que não dependem do @Valid da camada HTTP.
         */
        if (request == null || request.nome() == null || request.nome().isBlank() || request.nome().strip().length() > 120) {
            throw new DefaultErrorException("Informe o nome da matéria com até 120 caracteres.");
        }
        return request.nome().strip();
    }

    /**
     * Cadastra uma matéria pelo nome.
     *
     * @param request dados da nova matéria
     * @return matéria criada com seu identificador
     * @throws DefaultErrorException quando o nome é inválido ou já cadastrado
     */
    public MateriaResponse create(MateriaRequest request) {
        /*
         * O cadastro aceita somente um nome preenchido dentro do limite da coluna.
         * Espaços externos não fazem parte da identificação da matéria.
         */
        if (request == null || request.nome() == null || request.nome().isBlank() || request.nome().strip().length() > 120) {
            throw new DefaultErrorException("Informe o nome da matéria com até 120 caracteres.");
        }
        /*
         * A unicidade é garantida pelo banco, inclusive em cadastros simultâneos.
         * Uma tentativa repetida recebe conflito em vez de criar outra matéria.
         */
        try {
            var materia = repository.save(request.nome().strip());
            return new MateriaResponse(materia.id(), materia.nome());
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Esta matéria já está cadastrada.", HttpStatus.CONFLICT);
        }
    }
}

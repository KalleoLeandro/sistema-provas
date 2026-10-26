package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.*;
import br.com.provas.model.request.*;
import br.com.provas.model.response.*;
import br.com.provas.repository.ProfessorRepository;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mantém professores, matérias e turmas, reservando cada matéria/turma a um único professor ativo. */
@Service
public class ProfessorService {
    private final ProfessorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorService(ProfessorRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra professor ativo, matérias obrigatórias e as turmas específicas de cada matéria.
     * @param request dados validados pelo controller; cada matéria pode ter uma lista vazia de turmas
     * @return dados públicos com as atribuições cadastradas
     * @throws DefaultErrorException quando há referências inválidas, duplicação ou conflito entre professores ativos
     */
    @Transactional
    public ProfessorResponse create(ProfessorRequest request) {
        /*
         * Uma matéria só aparece uma vez no cadastro. As turmas são únicas dentro
         * de cada matéria, mas a mesma turma pode receber matérias diferentes.
         */
        validateMaterias(request.materias());
        /*
         * O banco aplica o status ativo por padrão, sem recebê-lo do formulário.
         * Somente o hash da senha é persistido. O conflito de login é tratado
         * separadamente do conflito de atribuição para orientar a correção do formulário.
         */
        Professor professor;
        try {
            professor = repository.save(request.nome(), request.login(), passwordEncoder.encode(request.senha()));
        } catch (DuplicateKeyException exception) {
            throw new DefaultErrorException("Este login já está cadastrado para um professor.", HttpStatus.CONFLICT);
        }
        /*
         * O banco reserva cada combinação matéria/turma para um único professor ativo,
         * inclusive em envios simultâneos. Os vínculos usam o status retornado pelo
         * banco. Um conflito desfaz também a conta criada nesta transação.
         */
        try {
            repository.saveMaterias(professor.id(), request.materias(), professor.ativo());
        } catch (DuplicateKeyException exception) {
            throw assignmentConflict();
        }
        return toResponse(professor, List.of(), repository.findMateriasByProfessorIds(List.of(professor.id()))
            .getOrDefault(professor.id(), List.of()));
    }

    /**
     * Lista professores com matérias e respectivas turmas.
     * @return professores ordenados por nome e sem hashes de senha
     */
    @Transactional(readOnly = true)
    public List<ProfessorResponse> findAll() {
        /*
         * Consultas em lote evitam buscas por linha. Os vínculos anteriores à associação
         * de matérias são preservados, sem inventar qual disciplina era ministrada.
         */
        var professores = repository.findAll();
        var ids = professores.stream().map(Professor::id).toList();
        var legados = repository.findTurmasByProfessorIds(ids);
        var materias = repository.findMateriasByProfessorIds(ids);
        return professores.stream().map(p -> toResponse(p, legados.getOrDefault(p.id(), List.of()),
            materias.getOrDefault(p.id(), List.of()))).toList();
    }

    /**
     * Consulta o cadastro público usado no dashboard do professor autenticado.
     * @param id identificador autorizado pelo controller administrativo ou pela sessão do próprio professor
     * @return matérias e turmas pertencentes ao professor da sessão
     * @throws DefaultErrorException quando o cadastro não existe
     */
    @Transactional(readOnly = true)
    public ProfessorResponse findById(Long id) {
        /* O endpoint do professor usa a identidade da sessão; a consulta por ID exige administrador. */
        var professor = repository.findById(id).orElseThrow(() ->
            new DefaultErrorException("Professor não encontrado.", HttpStatus.NOT_FOUND));
        return toResponse(professor, repository.findTurmasByProfessorIds(List.of(id)).getOrDefault(id, List.of()),
            repository.findMateriasByProfessorIds(List.of(id)).getOrDefault(id, List.of()));
    }

    /**
     * Ativa ou desativa o professor e suas reservas de matéria/turma na mesma transação.
     * @param id identificador do professor
     * @param request status desejado
     * @return cadastro público atualizado
     * @throws DefaultErrorException quando o professor não existe ou a ativação conflita com outro ativo
     */
    @Transactional
    public ProfessorResponse updateStatus(Long id, StatusRequest request) {
        /*
         * O bloqueio da conta serializa mudanças de status do mesmo professor.
         * Ao desativar, as combinações ficam livres; ao ativar, todas precisam estar
         * disponíveis. A restrição única do banco protege também ativações concorrentes.
         */
        var professor = repository.findByIdForUpdate(id).orElseThrow(() ->
            new DefaultErrorException("Professor não encontrado.", HttpStatus.NOT_FOUND));
        try {
            repository.updateMateriasStatus(id, request.ativo());
        } catch (DuplicateKeyException exception) {
            throw assignmentConflict();
        }
        /*
         * O status só é confirmado junto com todas as reservas. Se uma única turma
         * estiver ocupada, a transação não altera nem o status nem os demais vínculos.
         */
        repository.updateStatus(id, request.ativo());
        var atualizado = new Professor(professor.id(), professor.nome(), professor.login(), professor.senhaHash(), request.ativo());
        return toResponse(atualizado, repository.findTurmasByProfessorIds(List.of(id)).getOrDefault(id, List.of()),
            repository.findMateriasByProfessorIds(List.of(id)).getOrDefault(id, List.of()));
    }

    /**
     * Atualiza dados e atribuições mantendo status, identidade e histórico do professor.
     * @param id professor existente
     * @param request dados atuais e senha opcional; branco mantém a senha anterior
     * @return cadastro público atualizado
     */
    @Transactional
    public ProfessorResponse update(Long id,AtualizarProfessorRequest request){
        /* O bloqueio compartilha a ordem da alteração de status, evitando reservas inconsistentes. */
        var professor=repository.findByIdForUpdate(id).orElseThrow(()->new DefaultErrorException("Professor não encontrado.",HttpStatus.NOT_FOUND));
        validateMaterias(request.materias());
        /* Senha ausente conserva o hash atual; a resposta nunca revela credenciais. */
        var senha=request.senha()==null?professor.senhaHash():passwordEncoder.encode(request.senha());
        try{repository.update(id,request.nome(),request.login(),senha);}
        catch(DuplicateKeyException e){throw new DefaultErrorException("Este login já está cadastrado para um professor.",HttpStatus.CONFLICT);}
        /* Substitui somente atribuições cadastrais. O banco mantém a reserva exclusiva de matéria/turma
           para ativos; qualquer conflito reverte também login, nome e senha na mesma transação.
           Questões, liberações e cópias de provas respondidas permanecem vinculadas ao mesmo ID. */
        try{repository.replaceMaterias(id,request.materias(),professor.ativo());}
        catch(DuplicateKeyException e){throw assignmentConflict();}
        return findById(id);
    }

    /** @param materias atribuições propostas para cadastro ou edição */
    private void validateMaterias(List<ProfessorMateriaRequest> materias){
        /* Confere duplicações e referências antes de substituir os vínculos existentes. */
        var materiasIds = materias.stream().map(ProfessorMateriaRequest::materiaId).toList();
        if (new HashSet<>(materiasIds).size() != materiasIds.size()) {
            throw new DefaultErrorException("materias: Não informe a mesma matéria mais de uma vez.");
        }
        for (var materia : materias) {
            if (new HashSet<>(materia.turmas()).size() != materia.turmas().size()) {
                throw new DefaultErrorException("materias: Não repita turmas dentro da mesma matéria.");
            }
        }
        /*
         * Todas as referências são conferidas antes de criar a conta e permanecem
         * protegidas por leitura compartilhada até o término da transação.
         */
        var turmasIds = materias.stream().flatMap(m -> m.turmas().stream()).distinct().sorted().toList();
        if (repository.findExistingMateriaIds(materiasIds).size() != materiasIds.size()) {
            throw new DefaultErrorException("materias: Uma ou mais matérias não foram encontradas.");
        }
        if (repository.findExistingTurmaIds(turmasIds).size() != turmasIds.size()) {
            throw new DefaultErrorException("materias: Uma ou mais turmas não foram encontradas.");
        }
    }

    /** @return recusa de negócio para uma reserva já ocupada por outro professor ativo */
    private DefaultErrorException assignmentConflict() {
        /* A mesma mensagem cobre cadastro e reativação, sem expor detalhes do banco. */
        return new DefaultErrorException("Já existe um professor ativo ministrando uma das matérias em uma das turmas selecionadas. Altere os vínculos ou desative o professor que já ocupa essa combinação.", HttpStatus.CONFLICT);
    }

    /**
     * Monta a projeção pública das atribuições, preservando turmas de cadastros antigos.
     * @param professor cadastro persistido
     * @param legados vínculos anteriores ao cadastro por matéria
     * @param materias matérias com suas turmas específicas
     * @return dados seguros e lista agregada de turmas sem repetição
     */
    private ProfessorResponse toResponse(Professor professor, List<Turma> legados, List<ProfessorMateria> materias) {
        /* O array agregado mantém compatibilidade na leitura; as matérias guardam a associação exata. */
        Map<Long, Turma> turmas = new LinkedHashMap<>();
        legados.forEach(t -> turmas.put(t.id(), t));
        materias.forEach(m -> m.turmas().forEach(t -> turmas.put(t.id(), t)));
        return new ProfessorResponse(professor.id(), professor.nome(), professor.login(), professor.ativo(),
            turmas.values().stream().map(this::turmaResponse).toList(),
            materias.stream().map(m -> new ProfessorMateriaResponse(m.materiaId(), m.nome(),
                m.turmas().stream().map(this::turmaResponse).toList())).toList());
    }

    /** @param turma registro de turma @return dados públicos da turma */
    private TurmaResponse turmaResponse(Turma turma) {
        /* A identificação de ensino, ano e letra é mantida para exibição do vínculo. */
        return new TurmaResponse(turma.id(), turma.ensino(), turma.ano(), turma.letra());
    }
}

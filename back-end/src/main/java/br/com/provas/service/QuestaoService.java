package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Questao;
import br.com.provas.model.request.QuestaoRequest;
import br.com.provas.model.response.QuestaoResponse;
import br.com.provas.model.response.QuestaoResumoResponse;
import br.com.provas.model.response.QuestaoDestinoResponse;
import br.com.provas.model.response.TurmaResponse;
import java.util.HashSet;
import org.springframework.web.util.HtmlUtils;
import java.util.Objects;
import br.com.provas.model.request.AtualizarQuestaoRequest;
import br.com.provas.repository.QuestaoRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persiste questões do professor autenticado, sem executar sorteio ou publicação de provas. */
@Service
public class QuestaoService {
    private final QuestaoRepository repository;
    public QuestaoService(QuestaoRepository repository) { this.repository = repository; }

    /**
     * Grava uma questão para uma matéria/turma pertencente ao professor da sessão.
     * @param professorId identificador extraído da sessão pelo controller
     * @param request campos validados pelo Spring e conferidos conforme o tipo de questão
     * @return questão confirmada pelo banco
     * @throws DefaultErrorException quando o vínculo não é permitido ou o conteúdo está incompleto
     */
    @Transactional
    public QuestaoResponse create(Long professorId, QuestaoRequest request) {
        /* Criação e versão inicial são confirmadas na mesma transação. */
        var dados = validate(professorId, request);
        var questao = repository.save(professorId, dados);
        repository.saveVersion(questao.id());
        return toResponse(questao);
    }

    /**
     * Atualiza a versão não utilizada ou cria outra quando a atual já compõe uma prova respondida.
     * @param id questão que será alterada
     * @param professorId autor autenticado
     * @param request dados e versão carregada pelo formulário
     * @return conteúdo atual e identificadores de versão e revisão
     */
    @Transactional
    public QuestaoResponse update(Long id, Long professorId, AtualizarQuestaoRequest request) {
        /* O bloqueio serializa edições; outra autoria retorna a mesma ausência de um ID desconhecido. */
        repository.findByIdForUpdate(id, professorId).orElseThrow(() -> new DefaultErrorException("Questão não encontrada.", HttpStatus.NOT_FOUND));
        if (!Objects.equals(repository.findCurrentVersionId(id), request.versaoId())
                || !Objects.equals(repository.findVersionRevision(request.versaoId()), request.revisao())) {
            throw new DefaultErrorException("Esta questão foi alterada em outra sessão. Recarregue antes de salvar.", HttpStatus.CONFLICT);
        }
        /* A validação é a mesma do cadastro; a marca de uso pertence ao futuro módulo de provas. */
        var dados = validate(professorId, request.questao());
        var questao = repository.update(id, professorId, dados);
        /* Uma versão usada em prova respondida é preservada; as demais são atualizadas no mesmo registro. */
        if (repository.isVersionUsedInAnsweredExam(request.versaoId())) repository.saveVersion(id);
        else repository.updateUnusedVersion(id, request.versaoId());
        return toResponse(questao);
    }

    /** @param professorId autor autenticado @param request conteúdo proposto @return dados normalizados e autorizados */
    private QuestaoRequest validate(Long professorId, QuestaoRequest request) {
        /* A matéria continua obrigatória. O professor precisa ministrá-la no ensino/ano escolhido. */
        if (!repository.hasActiveYear(professorId, request.materiaId(), request.ensino(), request.ano())) {
            throw new DefaultErrorException("Você não possui vínculo ativo com esta matéria neste ensino e ano.", HttpStatus.FORBIDDEN);
        }
        /* O assunto é obrigatório e pertence à mesma matéria; a classificação nunca é texto livre. */
        if(request.assuntoId()==null||!repository.hasSubject(request.assuntoId(),request.materiaId()))throw new DefaultErrorException("Selecione um assunto da matéria escolhida.");
        /* Turmas específicas devem existir, não se repetir e pertencer ao mesmo ensino/ano. */
        if ("turmas".equals(request.abrangencia())) {
            if (request.turmas().isEmpty() || new HashSet<>(request.turmas()).size() != request.turmas().size()) {
                throw new DefaultErrorException("turmas: Selecione uma ou mais turmas sem repetições.");
            }
            var turmas = repository.findTurmasByIds(request.turmas(), professorId, request.materiaId());
            if (turmas.size() != request.turmas().size()) {
                throw new DefaultErrorException("Selecione apenas turmas em que você ministra esta matéria.", HttpStatus.FORBIDDEN);
            }
            if (turmas.stream().anyMatch(t -> !request.ensino().equals(t.ensino()) || !request.ano().equals(t.ano()))) {
                throw new DefaultErrorException("turmas: Todas as turmas devem pertencer ao ensino e ano selecionados.");
            }
        } else if (!request.turmas().isEmpty()) {
            throw new DefaultErrorException("turmas: Para ano inteiro, não envie turmas específicas.");
        }
        /* HTML vazio não representa um enunciado. Texto ou imagem são aceitos como no editor da tela. */
        if (!hasContent(request.enunciado())) throw new DefaultErrorException("enunciado: Preencha o enunciado da questão.");
        QuestaoRequest dados = request;
        if ("objetiva".equals(request.tipo())) {
            /* Questões objetivas preservam cinco alternativas e uma resposta correta explícita. */
            for (String letra : List.of("A", "B", "C", "D", "E")) {
                if (!hasContent(request.alternativas().get(letra))) throw new DefaultErrorException("alternativas: Preencha a alternativa " + letra + ".");
            }
            if (request.respostaCorreta() == null) throw new DefaultErrorException("respostaCorreta: Selecione a resposta correta.");
        } else {
            /* Conteúdo de alternativas ocultas na tela não é gravado em uma questão discursiva. */
            dados = new QuestaoRequest(request.materiaId(), request.ensino(), request.ano(), request.abrangencia(), request.turmas(), request.tipo(), request.peso(), request.enunciado(), Map.of(), null,request.assuntoId());
        }
        return dados;

    }

    /**
     * Consulta uma questão salva, restrita ao seu autor.
     * @param id identificador da questão
     * @param professorId autor obtido da sessão
     * @return questão persistida
     * @throws DefaultErrorException quando não existe questão acessível com esse ID
     */
    @Transactional(readOnly = true)
    public QuestaoResponse findById(Long id, Long professorId) {
        /* A mesma ausência é retornada para IDs desconhecidos ou pertencentes a outro professor. */
        return toResponse(repository.findById(id, professorId).orElseThrow(() ->
            new DefaultErrorException("Questão não encontrada.", HttpStatus.NOT_FOUND)));
    }

    /**
     * Lista as questões do autor autenticado, sem carregar imagens e alternativas completas.
     * @param professorId identificador obtido da sessão
     * @return resumos das questões, das mais recentes para as mais antigas
     */
    @Transactional(readOnly = true)
    public List<QuestaoResumoResponse> findAll(Long professorId) {
        /* O filtro por autor pertence à consulta; a página nunca determina qual professor será listado. */
        return repository.findAll(professorId).stream().map(q -> {
            /* Remove marcação e fragmentos de imagem truncados, sem devolver base64 na tabela. */
            String texto = HtmlUtils.htmlUnescape(q.enunciado()
                .replaceAll("(?is)<!--.*?-->|<(script|style|svg)\\b[^>]*>.*?</(script|style|svg)>", " ")
                .replaceAll("(?is)<[^>]*>", " ").replaceAll("(?s)<[^>]*$", ""))
                .replaceAll("[\\p{So}\\uFE0E\\uFE0F\\u200D]", "")
                .replace('\u00a0', ' ').replaceAll("\\s+", " ").strip();
            String resumo = texto.isEmpty() ? "Questão com imagem" : texto.length() > 160 ? texto.substring(0,157).stripTrailing() + "..." : texto;
            return new QuestaoResumoResponse(q.id(), q.materia(), q.ensino(), q.ano(), q.letras() == null ? "" : q.letras(), q.abrangencia(), q.tipo(), q.peso(), resumo,q.assuntoId(),q.assunto());
        }).toList();
    }

    /**
     * Lista matérias e anos autorizados, com suas turmas vinculadas nesses anos.
     * @param professorId autor autenticado
     * @return opções de matéria, ensino, ano e turma para o formulário
     */
    @Transactional(readOnly = true)
    public List<QuestaoDestinoResponse> options(Long professorId) {
        /* O escopo é a produção de questões, limitado às atribuições ativas do próprio professor. */
        return repository.findOptions(professorId).stream().map(d -> new QuestaoDestinoResponse(
            d.materiaId(), d.materia(), d.turmaId(), d.ensino(), d.ano(), d.letra())).toList();
    }

    /** @param html conteúdo do editor @return se há texto útil ou imagem */
    private boolean hasContent(String html) {
        /* Apenas identifica conteúdo; a renderização usa a sanitização padrão do Angular. */
        return html != null && (html.matches("(?is).*<img\\b[^>]*\\bsrc\\s*=.+")
            || !html.replaceAll("(?is)<[^>]*>", "").replace("&nbsp;", " ").replace("&#160;", " ")
                .replace("\u00a0", " ").replace("\u200b", "").isBlank());
    }

    /** @param questao registro confirmado no banco @return dados públicos da questão */
    private QuestaoResponse toResponse(Questao questao) {
        /* O autor continua determinado pela sessão; a resposta confirma o conteúdo efetivamente salvo. */
        /* Ano inteiro é resolvido na leitura e inclui somente turmas com vínculo ativo do autor nessa matéria/ano. */
        var turmas = repository.findTargets(questao).stream().map(t -> new TurmaResponse(t.id(), t.ensino(), t.ano(), t.letra())).toList();
        Long versaoId = repository.findCurrentVersionId(questao.id());
        return new QuestaoResponse(questao.id(), questao.materiaId(), questao.ensino(), questao.ano(), questao.abrangencia(), turmas, questao.tipo(), questao.peso(),
            questao.enunciado(), questao.alternativas(), questao.respostaCorreta(), versaoId, repository.findVersionRevision(versaoId),questao.assuntoId(),repository.subjectName(questao.assuntoId()));
    }
}

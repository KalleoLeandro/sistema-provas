package br.com.provas.service;

import br.com.provas.exceptions.DefaultErrorException;
import br.com.provas.model.entity.Questao;
import br.com.provas.model.entity.QuestaoResumo;
import br.com.provas.model.request.QuestaoRequest;
import br.com.provas.repository.QuestaoRepository;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestaoServiceTest {
    private final QuestaoRepository repository = mock(QuestaoRepository.class);
    private final QuestaoService service = new QuestaoService(repository);
    private QuestaoRequest request(String tipo, String enunciado, Map<String,String> alternativas) {
        return new QuestaoRequest(6L, "F", 1, "turmas", List.of(5L), tipo, BigDecimal.ONE, enunciado, alternativas, "C");
    }
    private Map<String,String> alternativas() { return Map.of("A","<p>1</p>","B","<p>2</p>","C","<p>3</p>","D","<p>4</p>","E","<p>5</p>"); }

    @Test void savesQuestionWithAuthorFromSession() {
        var request = request("objetiva", "<p>Enunciado</p>", alternativas());
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        when(repository.findTurmasByIds(List.of(5L),7L,6L)).thenReturn(List.of(new br.com.provas.model.entity.Turma(5L,"F",1,"A")));
        when(repository.save(7L,request)).thenReturn(new Questao(10L,7L,6L,"F",1,"turmas","objetiva",BigDecimal.ONE,request.enunciado(),alternativas(),"C"));
        assertEquals(10L,service.create(7L,request).id()); verify(repository).save(7L,request);
    }
    @Test void rejectsAnotherProfessorsAssignment() {
        assertEquals(HttpStatus.FORBIDDEN,assertThrows(DefaultErrorException.class,
            () -> service.create(7L,request("objetiva","<p>Enunciado</p>",alternativas()))).getStatus());
        verify(repository,never()).save(anyLong(),any());
    }
    @Test void rejectsEmptyRichTextAndMissingAlternative() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        when(repository.findTurmasByIds(List.of(5L),7L,6L)).thenReturn(List.of(new br.com.provas.model.entity.Turma(5L,"F",1,"A")));
        assertThrows(DefaultErrorException.class,() -> service.create(7L,request("objetiva","<p><br></p>",alternativas())));
        assertThrows(DefaultErrorException.class,() -> service.create(7L,request("objetiva","<p>Texto</p>",Map.of("A","<p>1</p>"))));
        verify(repository,never()).save(anyLong(),any());
    }
    @Test void discursiveQuestionDoesNotStoreHiddenAlternativesOrAnswerKey() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        when(repository.findTurmasByIds(List.of(5L),7L,6L)).thenReturn(List.of(new br.com.provas.model.entity.Turma(5L,"F",1,"A")));
        when(repository.save(eq(7L),any())).thenAnswer(call -> {
            QuestaoRequest dados = call.getArgument(1);
            assertTrue(dados.alternativas().isEmpty()); assertNull(dados.respostaCorreta());
            return new Questao(11L,7L,6L,"F",1,"turmas",dados.tipo(),dados.peso(),dados.enunciado(),Map.of(),null);
        });
        assertNull(service.create(7L,request("discursiva","<p>Texto</p>",alternativas())).respostaCorreta());
    }
    @Test void readIsRestrictedToTheAuthor() {
        when(repository.findById(10L,8L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,() -> service.findById(10L,8L)).getStatus());
        verify(repository).findById(10L,8L);
    }
    @Test void listUsesSessionAuthorAndRemovesMarkupAndPartialImages() {
        when(repository.findAll(7L)).thenReturn(List.of(
            new QuestaoResumo(11L,"Matemática","F",1,"A","turmas","objetiva",BigDecimal.ONE,"<p>Quanto é <b>2 + 2</b>?</p>"),
            new QuestaoResumo(10L,"Matemática","F",1,"A","turmas","discursiva",BigDecimal.ONE,"<img src=\"data:image/png;base64,abc")));
        var result = service.findAll(7L);
        assertEquals(2,result.size()); assertFalse(result.getFirst().resumo().contains("<"));
        assertEquals("Questão com imagem",result.get(1).resumo());
        verify(repository).findAll(7L);
    }
    @Test void rejectsClassesFromAnotherYearOrEducationLevel() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        var request = request("discursiva", "<p>Texto</p>", Map.of());
        for (var turma : List.of(new br.com.provas.model.entity.Turma(5L,"F",2,"A"), new br.com.provas.model.entity.Turma(5L,"M",1,"A"))) {
            when(repository.findTurmasByIds(List.of(5L),7L,6L)).thenReturn(List.of(turma));
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows(DefaultErrorException.class, () -> service.create(7L,request)).getStatus());
        }
        verify(repository,never()).save(anyLong(),any());
    }
    @Test void acceptsSeveralClassesAndWholeYearWithoutDuplicatingQuestions() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        var turmas = List.of(new br.com.provas.model.entity.Turma(5L,"F",1,"A"), new br.com.provas.model.entity.Turma(13L,"F",1,"B"));
        when(repository.findTurmasByIds(List.of(5L,13L),7L,6L)).thenReturn(turmas);
        when(repository.save(eq(7L),any())).thenAnswer(call -> {
            QuestaoRequest r = call.getArgument(1);
            return new Questao(20L,7L,6L,r.ensino(),r.ano(),r.abrangencia(),r.tipo(),r.peso(),r.enunciado(),Map.of(),null);
        });
        when(repository.findTargets(any())).thenReturn(turmas);
        var specific = new QuestaoRequest(6L,"F",1,"turmas",List.of(5L,13L),"discursiva",BigDecimal.ONE,"<p>Texto</p>",Map.of(),null);
        assertEquals(2,service.create(7L,specific).turmas().size());
        var year = new QuestaoRequest(6L,"F",1,"ano",List.of(),"discursiva",BigDecimal.ONE,"<p>Texto</p>",Map.of(),null);
        assertEquals("ano",service.create(7L,year).abrangencia());
        verify(repository,times(2)).save(eq(7L),any());
    }
    @Test void rejectsEmptyDuplicateAndHiddenClassSelections() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        for (var ids : List.of(List.<Long>of(),List.of(5L,5L))) {
            var r = new QuestaoRequest(6L,"F",1,"turmas",ids,"discursiva",BigDecimal.ONE,"<p>Texto</p>",Map.of(),null);
            assertThrows(DefaultErrorException.class,() -> service.create(7L,r));
        }
        var r = new QuestaoRequest(6L,"F",1,"ano",List.of(5L),"discursiva",BigDecimal.ONE,"<p>Texto</p>",Map.of(),null);
        assertThrows(DefaultErrorException.class,() -> service.create(7L,r));
        verify(repository,never()).save(anyLong(),any());
    }

    @Test void editingCreatesANewVersionAndKeepsTheQuestionIdentity() {
        var original = new Questao(10L,7L,6L,"F",1,"ano","discursiva",BigDecimal.ONE,"<p>Original</p>",Map.of(),null);
        when(repository.findByIdForUpdate(10L,7L)).thenReturn(Optional.of(original));
        when(repository.findCurrentVersionId(10L)).thenReturn(100L,101L);
        when(repository.findVersionRevision(100L)).thenReturn(1L);
        when(repository.isVersionUsedInAnsweredExam(100L)).thenReturn(true);
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        var edited = new QuestaoRequest(6L,"F",1,"ano",List.of(),"discursiva",new BigDecimal("2"),"<p>Alterada</p>",Map.of(),null);
        when(repository.update(eq(10L),eq(7L),any())).thenReturn(new Questao(10L,7L,6L,"F",1,"ano","discursiva",new BigDecimal("2"),edited.enunciado(),Map.of(),null));
        var result = service.update(10L,7L,new br.com.provas.model.request.AtualizarQuestaoRequest(100L,1L,edited));
        assertEquals(10L,result.id()); assertEquals(101L,result.versaoId()); assertEquals("<p>Alterada</p>",result.enunciado());
        assertEquals("<p>Original</p>",original.enunciado());
        var order = inOrder(repository); order.verify(repository).findByIdForUpdate(10L,7L);
        order.verify(repository).update(eq(10L),eq(7L),any()); order.verify(repository).saveVersion(10L);
        verify(repository,never()).save(anyLong(),any());
    }
    @Test void editingAnotherAuthorsQuestionIsDenied() {
        when(repository.findByIdForUpdate(10L,8L)).thenReturn(Optional.empty());
        var r = new br.com.provas.model.request.AtualizarQuestaoRequest(100L,1L,request("discursiva","Texto",Map.of()));
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(DefaultErrorException.class,()->service.update(10L,8L,r)).getStatus());
        verify(repository,never()).update(anyLong(),anyLong(),any()); verify(repository,never()).saveVersion(anyLong());
    }
    @Test void staleEditDoesNotChangeTheQuestionOrCreateAVersion() {
        when(repository.findByIdForUpdate(10L,7L)).thenReturn(Optional.of(new Questao(10L,7L,6L,"F",1,"ano","discursiva",BigDecimal.ONE,"Original",Map.of(),null)));
        when(repository.findCurrentVersionId(10L)).thenReturn(101L);
        var r = new br.com.provas.model.request.AtualizarQuestaoRequest(100L,1L,request("discursiva","Alterada",Map.of()));
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.update(10L,7L,r)).getStatus());
        verify(repository,never()).update(anyLong(),anyLong(),any()); verify(repository,never()).saveVersion(anyLong());
    }

    @Test void unusedVersionIsUpdatedWithoutCreatingHistory() {
        when(repository.findByIdForUpdate(10L,7L)).thenReturn(Optional.of(new Questao(10L,7L,6L,"F",1,"ano","discursiva",BigDecimal.ONE,"Original",Map.of(),null)));
        when(repository.findCurrentVersionId(10L)).thenReturn(100L);
        when(repository.findVersionRevision(100L)).thenReturn(1L,2L);
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        var r = new QuestaoRequest(6L,"F",1,"ano",List.of(),"discursiva",BigDecimal.ONE,"Alterada",Map.of(),null);
        when(repository.update(eq(10L),eq(7L),any())).thenReturn(new Questao(10L,7L,6L,"F",1,"ano","discursiva",BigDecimal.ONE,"Alterada",Map.of(),null));
        var result = service.update(10L,7L,new br.com.provas.model.request.AtualizarQuestaoRequest(100L,1L,r));
        assertEquals(100L,result.versaoId()); assertEquals(2L,result.revisao());
        verify(repository).updateUnusedVersion(10L,100L); verify(repository,never()).saveVersion(anyLong());
    }
    @Test void staleRevisionOfTheSameVersionIsRejected() {
        when(repository.findByIdForUpdate(10L,7L)).thenReturn(Optional.of(new Questao(10L,7L,6L,"F",1,"ano","discursiva",BigDecimal.ONE,"Original",Map.of(),null)));
        when(repository.findCurrentVersionId(10L)).thenReturn(100L);
        when(repository.findVersionRevision(100L)).thenReturn(2L);
        var r = new br.com.provas.model.request.AtualizarQuestaoRequest(100L,1L,request("discursiva","Alterada",Map.of()));
        assertEquals(HttpStatus.CONFLICT,assertThrows(DefaultErrorException.class,()->service.update(10L,7L,r)).getStatus());
        verify(repository,never()).update(anyLong(),anyLong(),any());
    }

    @Test void rejectsAClassOfTheSameYearWithoutAnAssignmentToTheSubject() {
        when(repository.hasActiveYear(7L,6L,"F",1)).thenReturn(true);
        when(repository.findTurmasByIds(List.of(13L),7L,6L)).thenReturn(List.of());
        var r=new QuestaoRequest(6L,"F",1,"turmas",List.of(13L),"discursiva",BigDecimal.ONE,"Texto",Map.of(),null);
        assertEquals(HttpStatus.FORBIDDEN,assertThrows(DefaultErrorException.class,()->service.create(7L,r)).getStatus());
        verify(repository,never()).save(anyLong(),any());
    }

    @Test void summaryContainsOnlyTextAndEllipsisWhenTruncated() {
        when(repository.findAll(7L)).thenReturn(List.of(
            new QuestaoResumo(1L,"Matemática","F",1,"A","turmas","objetiva",BigDecimal.ONE,"<p>Calcule ★ 2 + 2 &amp; explique.</p><img src='data:image/png;base64,abc'>"),
            new QuestaoResumo(2L,"Matemática","F",1,"A","turmas","discursiva",BigDecimal.ONE,"<p>"+"Texto longo ".repeat(30)+"</p>")));
        var result=service.findAll(7L);
        assertEquals("Calcule 2 + 2 & explique.",result.getFirst().resumo());
        assertTrue(result.get(1).resumo().endsWith("...")); assertTrue(result.get(1).resumo().length()<=160);
        assertFalse(result.getFirst().resumo().contains("base64"));
    }

}

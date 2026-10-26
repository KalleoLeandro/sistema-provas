package br.com.provas.model.response;
import java.util.List;
public record ProvaCorrecaoResponse(ProvaCorrecaoResumoResponse prova,List<QuestaoCorrecaoResponse> questoes) {}

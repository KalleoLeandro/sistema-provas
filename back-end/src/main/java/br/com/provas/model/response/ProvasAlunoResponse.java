package br.com.provas.model.response;
public record ProvasAlunoResponse(PaginaResponse<ProvaAlunoResponse> provas,long total,long concluidas) {}

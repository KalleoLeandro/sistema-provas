package br.com.provas.model.response;

import java.util.List;

public record ValidacaoErroResponse(String mensagem, List<CampoErroResponse> erros) {}

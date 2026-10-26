package br.com.provas.model.response;
import java.util.List;
public record ImportacaoAlunosResponse(int total,int validos,int importados,List<LinhaImportacaoAlunoResponse> linhas,List<ErroImportacaoAlunoResponse> erros) {}

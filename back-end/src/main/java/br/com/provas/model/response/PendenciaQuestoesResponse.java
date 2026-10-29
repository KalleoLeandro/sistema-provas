package br.com.provas.model.response;
import java.util.List;
public record PendenciaQuestoesResponse(Long materiaId,String materia,Long turmaId,String ensino,Integer ano,String letra,List<QuantidadeQuestoesResponse> quantidades,Long assuntoId,String assunto,Long liberacaoId,String titulo){}

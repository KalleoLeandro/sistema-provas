package br.com.provas.model.response;
import java.util.List;
public record AssuntoProvaResponse(Long id,Long materiaId,String nome,List<QuantidadeQuestoesResponse> quantidades,boolean completo){}

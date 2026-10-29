package br.com.provas.model.entity;
import java.math.BigDecimal;
public record PendenciaComposicao(Long liberacaoId,String titulo,Long materiaId,String materia,Long turmaId,String ensino,Integer ano,String letra,Long assuntoId,String assunto,BigDecimal peso,int minimo,int cadastradas){}

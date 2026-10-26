package br.com.provas.model.response;
import java.util.List;
public record PaginaResponse<T>(List<T> itens, long total, int pagina, int tamanho) {}

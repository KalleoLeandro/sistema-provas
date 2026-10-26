package br.com.provas.model.request;
import jakarta.validation.constraints.*;
public record SalvarRespostaRequest(@Size(max=10000,message="A resposta deve conter até 10.000 caracteres.") String resposta,
 @NotNull(message="Informe a revisão da resposta.") @PositiveOrZero(message="Revisão inválida.") Long revisao) {}

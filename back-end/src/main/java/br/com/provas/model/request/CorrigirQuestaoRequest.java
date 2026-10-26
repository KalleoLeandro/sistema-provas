package br.com.provas.model.request;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
public record CorrigirQuestaoRequest(
 @NotNull(message="Informe a pontuação da questão.") @DecimalMin(value="0",message="A pontuação não pode ser negativa.") @Digits(integer=2,fraction=2,message="Use até duas casas decimais na pontuação.") BigDecimal pontuacao,
 @Size(max=3000,message="O feedback deve conter até 3.000 caracteres.") String feedback,
 @NotNull(message="Informe a revisão da correção.") @PositiveOrZero(message="Revisão inválida.") Long revisao
){public CorrigirQuestaoRequest{feedback=feedback==null||feedback.isBlank()?null:feedback.strip();}}

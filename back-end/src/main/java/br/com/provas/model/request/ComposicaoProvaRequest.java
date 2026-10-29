package br.com.provas.model.request;
import jakarta.validation.constraints.*;import java.math.BigDecimal;
public record ComposicaoProvaRequest(@NotNull @Positive Long assuntoId,@NotNull @DecimalMin("0.01") @Digits(integer=6,fraction=2) BigDecimal peso,@NotNull @Min(1) @Max(200) Integer quantidade){}

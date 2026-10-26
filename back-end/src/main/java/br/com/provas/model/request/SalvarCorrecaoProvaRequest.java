package br.com.provas.model.request;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record SalvarCorrecaoProvaRequest(
 @NotEmpty(message="Informe as questões da prova.") @Size(max=10,message="A correção deve conter até dez questões.") List<@NotNull @Valid Item> questoes
){public record Item(@NotNull(message="Informe o número da questão.") @Min(1) @Max(10) Integer numero,@NotNull(message="Informe a correção da questão.") @Valid CorrigirQuestaoRequest correcao){}}

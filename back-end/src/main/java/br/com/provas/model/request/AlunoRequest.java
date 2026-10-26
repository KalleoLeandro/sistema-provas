package br.com.provas.model.request;

import jakarta.validation.constraints.*;
import java.util.Locale;
import java.math.BigDecimal;

public record AlunoRequest(
    @NotBlank(message="O nome é obrigatório.") @Size(max=120,message="O nome deve ter até 120 caracteres.") String nome,
    @Positive(message="O número deve ser um inteiro positivo.") @Digits(integer=10,fraction=0,message="O número deve ser inteiro.") @DecimalMax(value="2147483647",message="O número excede o limite permitido.") BigDecimal numero,
    @NotBlank(message="O CGM é obrigatório.") @Size(max=30,message="O CGM deve ter até 30 dígitos.") @Pattern(regexp="[0-9]+",message="O CGM deve conter somente números.") String cgm,
    @NotBlank(message="O status é obrigatório.") @Pattern(regexp="M|T",message="Selecione Matriculado ou Transferido.") String status,
    @NotNull(message="A turma é obrigatória.") @Positive(message="Selecione uma turma válida.") Long turmaId
) {
    public AlunoRequest {
        nome=nome==null?null:nome.strip(); cgm=cgm==null?null:cgm.strip();
        status=status==null?null:status.strip().toUpperCase(Locale.ROOT);
    }
}

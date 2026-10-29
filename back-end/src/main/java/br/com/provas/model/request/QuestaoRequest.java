package br.com.provas.model.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.List;

public record QuestaoRequest(
    @NotNull(message = "A matéria é obrigatória.") @Positive(message = "Informe uma matéria válida.") Long materiaId,
    @NotBlank(message = "O ensino é obrigatório.") @Pattern(regexp = "F|M", message = "Selecione Fundamental ou Médio.") String ensino,
    @NotNull(message = "O ano é obrigatório.") @Min(value = 1, message = "O ano deve estar entre 1 e 9.") @Max(value = 9, message = "O ano deve estar entre 1 e 9.") Integer ano,
    @NotBlank(message = "A abrangência é obrigatória.") @Pattern(regexp = "turmas|ano", message = "Selecione turmas específicas ou ano inteiro.") String abrangencia,
    @NotNull(message = "Informe a lista de turmas, vazia para ano inteiro.") @Size(max = 26, message = "Selecione até 26 turmas do mesmo ano.")
    List<@NotNull(message = "A turma não pode ser nula.") @Positive(message = "Informe uma turma válida.") Long> turmas,
    @NotBlank(message = "O tipo é obrigatório.") @Pattern(regexp = "objetiva|discursiva", message = "Selecione um tipo de questão válido.") String tipo,
    @NotNull(message = "O peso é obrigatório.") @DecimalMin(value = "0.01", message = "O peso deve ser positivo.")
    @Digits(integer = 6, fraction = 2, message = "O peso deve ter até seis inteiros e duas casas decimais.") BigDecimal peso,
    @NotBlank(message = "O enunciado é obrigatório.") @Size(max = 8000000, message = "O enunciado excede o tamanho permitido.") String enunciado,
    @NotNull(message = "Informe as alternativas, mesmo quando vazias na questão discursiva.") @Size(max = 5, message = "Informe no máximo cinco alternativas.")
    Map<@NotBlank(message = "Identifique a alternativa de A a E.") @Pattern(regexp = "[A-E]", message = "A alternativa deve ser identificada de A a E.") String,
        @NotNull(message = "A alternativa não pode ser nula.") @Size(max = 8000000, message = "A alternativa excede o tamanho permitido.") String> alternativas,
    @Pattern(regexp = "[A-E]", message = "Selecione uma resposta correta de A a E.") String respostaCorreta
, @NotNull(message="Selecione o assunto da questão.") @Positive(message="Selecione um assunto válido.") Long assuntoId) {
    public QuestaoRequest {
        ensino = ensino == null ? null : ensino.strip().toUpperCase(Locale.ROOT);
        abrangencia = abrangencia == null ? null : abrangencia.strip().toLowerCase(Locale.ROOT);
        tipo = tipo == null ? null : tipo.strip().toLowerCase(Locale.ROOT);
        respostaCorreta = respostaCorreta == null ? null : respostaCorreta.strip().toUpperCase(Locale.ROOT);
    }
}

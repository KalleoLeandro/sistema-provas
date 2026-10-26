package br.com.provas.model.request;
import jakarta.validation.constraints.*;
public record ImportarAlunosRequest(
    @NotBlank(message="Selecione um CSV preenchido.") @Size(max=524288,message="O CSV deve ter até 512 KB.") String csv
) {}

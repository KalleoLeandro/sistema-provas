package br.com.provas.model.request;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
public record PlanejarProvaRequest(@NotNull(message="Selecione a matéria.") @Positive Long materiaId,@NotNull(message="Informe a configuração da prova.") @Valid ConfiguracaoProvaRequest configuracao){}

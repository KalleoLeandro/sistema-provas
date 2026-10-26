package br.com.provas.model.request;
import jakarta.validation.constraints.*;
public record SubmeterProvaRequest(@NotNull(message="Informe a confirmação de envio.") Boolean confirmarIncompleta) {}

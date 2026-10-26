package br.com.provas.model.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record LiberarProvaRequest(
    @NotBlank(message="Informe o título da prova.") @Size(max=120,message="O título deve ter até 120 caracteres.") String titulo,
    @NotNull(message="Selecione a matéria.") @Positive(message="Selecione uma matéria válida.") Long materiaId,
    @NotNull(message="Informe quando a prova ficará disponível.") Instant disponivelEm,
    @NotNull(message="Informe quando o prazo termina.") @Future(message="O encerramento deve estar no futuro.") Instant encerraEm,
    @NotNull(message="Informe a duração.") @Min(value=1,message="A duração mínima é de 1 minuto.") @Max(value=480,message="A duração máxima é de 480 minutos.") Integer duracaoMinutos,
    @Size(min=4,max=64,message="O token deve conter de 4 a 64 caracteres.") String token
) {
    public LiberarProvaRequest(String titulo,Long materiaId,Instant disponivelEm,Instant encerraEm){this(titulo,materiaId,disponivelEm,encerraEm,60,"TokenTeste");}
    @Override public String toString(){return "LiberarProvaRequest[token omitido]";}
    public LiberarProvaRequest { token=token==null||token.isBlank()?null:token.strip();titulo=titulo==null?null:titulo.strip(); }
    @AssertTrue(message="O encerramento deve ser posterior ao início da disponibilidade.")
    public boolean isPeriodoValido() { return disponivelEm==null || encerraEm==null || encerraEm.isAfter(disponivelEm); }
}

package br.com.provas.model.request;
import jakarta.validation.constraints.*;
public record IniciarProvaRequest(@NotBlank(message="Informe o token fornecido pelo professor.") @Size(min=4,max=64,message="O token deve conter de 4 a 64 caracteres.") String token,
 @NotNull(message="Confirme a leitura das regras.") @AssertTrue(message="Confirme a leitura das regras.") Boolean aceitouRegras){public IniciarProvaRequest{token=token==null?null:token.strip();}@Override public String toString(){return "IniciarProvaRequest[token omitido]";}}

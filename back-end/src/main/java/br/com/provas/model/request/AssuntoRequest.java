package br.com.provas.model.request;
import jakarta.validation.constraints.*;
public record AssuntoRequest(@NotNull(message="Selecione a matéria.") @Positive(message="Selecione uma matéria válida.") Long materiaId,
 @NotBlank(message="Informe o nome do assunto.") @Size(max=120,message="O assunto deve ter até 120 caracteres.") String nome){
 public AssuntoRequest{nome=nome==null?null:nome.strip().replaceAll("\\s+"," ");}
}

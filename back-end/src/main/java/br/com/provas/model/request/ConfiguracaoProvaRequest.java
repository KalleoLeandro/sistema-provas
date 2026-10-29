package br.com.provas.model.request;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.util.List;
public record ConfiguracaoProvaRequest(
 @NotNull(message="Informe o valor total da prova.") @DecimalMin(value="0.01",message="O valor da prova deve ser positivo.") @Digits(integer=9,fraction=2,message="Use até duas casas decimais no valor da prova.") BigDecimal valorTotal,
 @NotNull(message="Informe a quantidade de questões.") @Min(value=3,message="A prova deve ter pelo menos 3 questões.") @Max(value=200,message="A prova pode ter até 200 questões.") Integer quantidadeQuestoes,
 @NotEmpty(message="Selecione pelo menos um assunto.") @Size(max=10,message="Selecione até dez assuntos.") List<@NotNull @Valid Assunto> assuntos){
 public record Assunto(@NotNull @Positive Long assuntoId,
  @NotEmpty(message="Informe os pesos permitidos para o assunto.") @Size(max=20,message="Informe até vinte pesos por assunto.") List<@NotNull @DecimalMin(value="0.01",message="O peso deve ser positivo.") @Digits(integer=6,fraction=2,message="Use até duas casas decimais no peso.") BigDecimal> pesos,
  Boolean exigirTodosPesos){
  public Assunto { exigirTodosPesos=Boolean.TRUE.equals(exigirTodosPesos); }
  public Assunto(Long assuntoId,List<BigDecimal> pesos){this(assuntoId,pesos,false);}
 }
}

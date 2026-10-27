import { statusAluno } from '../../../models/status-aluno';
import { Component, EventEmitter, HostListener, Input, OnDestroy, Output, ViewChild, ElementRef, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AlertComponent, ButtonDirective, FieldComponent, InputDirective, RecordNameDirective } from '../../../design-system/components';
import { InfoTooltipComponent } from '../../../design-system/info-tooltip';
import { PaginationComponent } from '../../../design-system/pagination';
import { TableScrollComponent } from '../../../design-system/table-scroll';
import { ImportacaoAlunos } from '../../../models/importacao-alunos';
import { ApiService } from '../../../services/api.service';

@Component({selector:'app-importar-alunos',imports:[AlertComponent,ButtonDirective,FieldComponent,InputDirective,RecordNameDirective,InfoTooltipComponent,PaginationComponent,TableScrollComponent],
  templateUrl:'./importar-alunos.html',styleUrl:'../../../design-system/csv-import.css',
})
export class ImportarAlunosComponent implements OnDestroy {
 readonly statusAluno=statusAluno;
  private readonly api=inject(ApiService);
  @Input() disabled=false;
  @Output() busyChange=new EventEmitter<boolean>();
  @ViewChild('fileInput',{static:true}) fileInput!:ElementRef<HTMLInputElement>;
  readonly busy=signal(false);
  readonly erro=signal('');
  readonly arquivo=signal('');
  readonly resultado=signal<ImportacaoAlunos|null>(null);
  private csv='';private visit=0;private destroyed=false;
  pagina=1;tamanho=15;paginaErros=1;tamanhoErros=15;
  get hasFile(){return !!this.csv;}
  get podeImportar(){const r=this.resultado();return !!r && r.validos>0 && !r.erros.length && !r.importados && this.hasFile;}
  get linhas(){return (this.resultado()?.linhas??[]).slice((this.pagina-1)*this.tamanho,this.pagina*this.tamanho);}
  get erros(){return (this.resultado()?.erros??[]).slice((this.paginaErros-1)*this.tamanhoErros,this.paginaErros*this.tamanhoErros);}
  private setBusy(value:boolean){this.busy.set(value);if(!this.destroyed)this.busyChange.emit(value);}

  /** Lê o CSV somente em memória e invalida a prévia anterior ao selecionar outro arquivo. */
  async selectFile(event:Event){
    if(this.busy()||this.disabled)return;
    const file=(event.target as HTMLInputElement).files?.[0];this.clear();
    if(!file)return;
    if(!file.name.toLowerCase().endsWith('.csv')){this.erro.set('Selecione um arquivo .csv.');return;}
    if(!file.size||file.size>524288){this.erro.set('Selecione um CSV preenchido de até 512 KB.');return;}
    const visit=++this.visit;this.setBusy(true);
    try{
      const text=await file.text();if(visit!==this.visit)return;
      if(text.includes('\uFFFD'))throw new Error('Exporte o CSV em UTF-8 para preservar os nomes.');
      this.csv=text;this.arquivo.set(file.name);
    }catch(error){if(visit===this.visit)this.erro.set(error instanceof Error?error.message:'Não foi possível ler o arquivo.');}
    finally{if(visit===this.visit)this.setBusy(false);}
  }
  /** Solicita a prévia sem gravar, identificando os problemas por linha e coluna. */
  async validate(){
    if(this.busy()||this.disabled||!this.hasFile)return;
    this.resultado.set(null);this.erro.set('');this.pagina=1;this.paginaErros=1;
    const visit=this.visit;this.setBusy(true);
    try{const result=await this.api.post<ImportacaoAlunos>('/alunos/importar/validar',{csv:this.csv});if(visit===this.visit)this.resultado.set(result);}
    catch(error){if(visit===this.visit)this.handleError(error);}
    finally{if(visit===this.visit)this.setBusy(false);}
  }
  /** Confirma o lote validado; o servidor repete a conferência antes de gravar a transação. */
  async import(){
    if(this.busy()||this.disabled||!this.podeImportar)return;
    const visit=this.visit;this.setBusy(true);this.erro.set('');
    try{
      const result=await this.api.post<ImportacaoAlunos>('/alunos/importar',{csv:this.csv});
      if(visit!==this.visit)return;
      this.resultado.set(result);this.csv='';this.arquivo.set('');this.fileInput.nativeElement.value='';
    }catch(error){if(visit===this.visit)this.handleError(error);}
    finally{if(visit===this.visit)this.setBusy(false);}
  }
  private handleError(error:unknown){
    const data=error instanceof HttpErrorResponse?error.error:null;
    if(Array.isArray(data?.linhas)&&Array.isArray(data?.erros)){this.resultado.set(data);this.erro.set('');}
    else{this.resultado.set(null);this.erro.set(data?.mensagem||'Não foi possível processar o CSV. Tente novamente.');}
  }
  /** Limpa arquivo e relatório, sem manter dados do aluno no navegador. */
  clear(){
    if(this.busy()||this.disabled)return;
    this.discard();
  }
  @HostListener('window:pagehide') discard(){
    this.visit++;this.csv='';this.arquivo.set('');this.resultado.set(null);this.erro.set('');
    this.pagina=1;this.paginaErros=1;if(this.fileInput)this.fileInput.nativeElement.value='';this.setBusy(false);
  }
  ngOnDestroy(){this.destroyed=true;this.discard();}
}

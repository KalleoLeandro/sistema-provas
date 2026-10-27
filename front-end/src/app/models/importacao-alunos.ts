export interface ImportacaoAlunos {
  total:number;validos:number;importados:number;
  linhas:{linha:number;nome:string;numero:number|null;cgm:string;status:'M'|'T'|'R'|'D';turmaId:number;ensino:'F'|'M';ano:number;letra:string}[];
  erros:{linha:number;campo:string;mensagem:string}[];
}

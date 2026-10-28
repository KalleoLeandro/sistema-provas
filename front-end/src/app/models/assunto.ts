export interface Assunto {id:number;materiaId:number;nome:string;}
export interface AssuntoProva extends Assunto {quantidades:{peso:number;minimo:number;cadastradas:number;faltantes:number}[];completo:boolean;}

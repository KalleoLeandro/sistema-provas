export type StatusAluno = 'M'|'T'|'R'|'D';
export const STATUS_ALUNO:Record<StatusAluno,string>={M:'Matriculado',T:'Transferido',R:'Remanejado',D:'Desistente'};
export function statusAluno(status:string):string{return STATUS_ALUNO[status as StatusAluno]??status;}

/** Participação e desempenho desconsideram transferidos e desistentes. */
export function participaEstatisticas(status:string):boolean{return status==='M'||status==='R';}

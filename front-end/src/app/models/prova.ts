import { Assunto } from './assunto';
export interface LiberacaoProva {assuntos:Assunto[];
  duracaoMinutos:number;tokenConfigurado:boolean;id: number; titulo: string; materiaId: number; materia: string;
  disponivelEm: string; encerraEm: string; situacao: 'agendada' | 'disponivel' | 'encerrada';
}

export interface LiberacaoDetalhe {assuntos:Assunto[];id:number;titulo:string;materiaId:number;materia:string;disponivelEm:string;encerraEm:string;duracaoMinutos:number;token:string|null;precisaNovoToken:boolean;}

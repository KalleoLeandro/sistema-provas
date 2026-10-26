export interface LiberacaoProva {
  duracaoMinutos:number;tokenConfigurado:boolean;id: number; titulo: string; materiaId: number; materia: string;
  disponivelEm: string; encerraEm: string; situacao: 'agendada' | 'disponivel' | 'encerrada';
}

export interface LiberacaoDetalhe {id:number;titulo:string;materiaId:number;materia:string;disponivelEm:string;encerraEm:string;duracaoMinutos:number;token:string|null;precisaNovoToken:boolean;}

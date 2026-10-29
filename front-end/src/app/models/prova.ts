import { Assunto } from './assunto';
export interface LiberacaoProva {valorTotal:number;quantidadeQuestoes:number;assuntos:Assunto[];
  duracaoMinutos:number;tokenConfigurado:boolean;id: number; titulo: string; materiaId: number; materia: string;
  disponivelEm: string; encerraEm: string; situacao: 'agendada' | 'disponivel' | 'encerrada';
}

export interface LiberacaoDetalhe {configuracao:ConfiguracaoProva|null;composicao:ComposicaoProva[];valorTotal:number;quantidadeQuestoes:number;assuntos:Assunto[];id:number;titulo:string;materiaId:number;materia:string;disponivelEm:string;encerraEm:string;duracaoMinutos:number;token:string|null;precisaNovoToken:boolean;}

export interface ConfiguracaoProva {valorTotal:number;quantidadeQuestoes:number;assuntos:{assuntoId:number;pesos:number[];exigirTodosPesos?:boolean}[];}
export interface ComposicaoProva {assuntoId:number;peso:number;quantidade:number;}
export interface PlanejamentoProva {possivel:boolean;motivo:string;faltantes:number|null;composicao:(ComposicaoProva&{assunto:string;disponiveis:number;faltantes:number})[];}

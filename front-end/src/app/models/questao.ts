import { Turma } from './turma';
export type LetraAlternativa = 'A' | 'B' | 'C' | 'D' | 'E';
export interface QuestaoRascunho {
  id: number;
  tipo: 'objetiva' | 'discursiva';
  peso: number;
  enunciado: string;
  alternativas: Record<LetraAlternativa, string>;
  respostaCorreta: LetraAlternativa;
}
export interface QuestaoResponse {
 assuntoId:number;assunto:string;
  versaoId: number;
  revisao: number;
  id: number;
  materiaId: number;
  ensino: 'F' | 'M';
  ano: number;
  abrangencia: 'turmas' | 'ano';
  turmas: Turma[];
  tipo: 'objetiva' | 'discursiva';
  peso: number;
  enunciado: string;
  alternativas: Partial<Record<LetraAlternativa, string>>;
  respostaCorreta: LetraAlternativa | null;
}
export interface QuestaoCadastrada {
  questao: QuestaoRascunho;
  vinculo: string;
  resumo: string;
}
export interface QuestaoResumo {
 assuntoId:number;assunto:string;
  id: number;
  materia: string;
  ensino: 'F' | 'M';
  ano: number;
  letras: string;
  abrangencia: 'turmas' | 'ano';
  tipo: 'objetiva' | 'discursiva';
  peso: number;
  resumo: string;
}

export interface QuestaoDestino { materiaId: number; materia: string; turmaId: number; ensino: 'F' | 'M'; ano: number; letra: string; }

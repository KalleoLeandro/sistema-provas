import { Turma } from './turma';

export interface Professor {
  id: number;
  nome: string;
  login: string;
  ativo: boolean;
  turmas: Turma[];
  materias: { materiaId: number; nome: string; turmas: Turma[] }[];
}

export type Ensino = 'F' | 'M';
export interface Turma {
  id: number;
  ensino: Ensino;
  ano: number;
  letra: string;
}

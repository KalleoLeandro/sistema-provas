export interface PendenciaQuestoes {
  materiaId: number;
  materia: string;
  turmaId: number;
  ensino: 'F' | 'M';
  ano: number;
  letra: string;
  quantidades: { peso: number; minimo: number; cadastradas: number; faltantes: number }[];
}

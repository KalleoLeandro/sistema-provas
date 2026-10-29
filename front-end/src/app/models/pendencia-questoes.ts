export interface PendenciaQuestoes {
 liberacaoId:number;titulo:string;
 assuntoId:number|null;assunto:string|null;
  materiaId: number;
  materia: string;
  turmaId: number;
  ensino: 'F' | 'M';
  ano: number;
  letra: string;
  quantidades: { peso: number; minimo: number; cadastradas: number; faltantes: number }[];
}

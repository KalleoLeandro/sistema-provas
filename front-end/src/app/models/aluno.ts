export interface Aluno { id: number; nome: string; numero: number | null; cgm: string; status: 'M' | 'T'; turmaId: number; ensino: 'F' | 'M'; ano: number; letra: string; }
export interface Pagina<T> { itens: T[]; total: number; pagina: number; tamanho: number; }

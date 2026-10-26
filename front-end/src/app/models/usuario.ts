export type Perfil = 'aluno' | 'professor' | 'administrador';

export interface Usuario {
  id: number;
  nome: string;
  login: string;
  perfil: Perfil;
}

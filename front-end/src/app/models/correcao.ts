export interface ProvaCorrecaoResumo {id:number;liberacaoId:number;titulo:string;materia:string;aluno:string;cgm:string;turmaId:number;ensino:'F'|'M';ano:number;letra:string;entregueEm:string;pendentes:number;notaParcial:number;}
export interface QuestaoCorrecao {numero:number;tipo:'objetiva'|'discursiva';peso:number;enunciado:string;alternativas:Record<string,string>;respostaCorreta:string|null;resposta:string|null;pontuacao:number|null;feedback:string|null;revisao:number;corrigidaEm:string|null;}
export interface ProvaCorrecao {prova:ProvaCorrecaoResumo;questoes:QuestaoCorrecao[];}
export interface RascunhoCorrecao {pontuacao:number|null;feedback:string;revisao:number;}

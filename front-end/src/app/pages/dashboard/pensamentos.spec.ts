import { PENSAMENTOS,pensamentoDoDia } from './pensamentos';
describe('Pensamento do dia',()=>{
 it('possui exatamente cem frases distintas',()=>{expect(PENSAMENTOS.length).toBe(100);expect(new Set(PENSAMENTOS).size).toBe(100);expect(PENSAMENTOS.every(f=>f.trim().length>0)).toBe(true);});
 it('mantém a frase no dia civil de Brasília e troca ao chegar a meia-noite',()=>{
  const inicio=pensamentoDoDia(new Date('2026-10-27T03:00:00Z'));
  expect(pensamentoDoDia(new Date('2026-10-28T02:59:59Z'))).toBe(inicio);
  expect(pensamentoDoDia(new Date('2026-10-28T03:00:00Z'))).not.toBe(inicio);
 });
 it('completa o ciclo sem perder datas anteriores à referência',()=>{
  expect(pensamentoDoDia(new Date('2026-01-01T12:00:00Z'))).toBe(PENSAMENTOS[0]);
  expect(pensamentoDoDia(new Date('2026-04-11T12:00:00Z'))).toBe(PENSAMENTOS[0]);
  expect(pensamentoDoDia(new Date('2025-12-31T12:00:00Z'))).toBe(PENSAMENTOS[99]);
 });
});

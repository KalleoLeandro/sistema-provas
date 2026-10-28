import { ListSort } from './list-sort';
describe('Ordenação das listas',()=>{
 it('alterna a direção e ordena números antes de paginar sem modificar a origem',()=>{
  const sort=new ListSort(),rows=[{id:1,numero:10},{id:2,numero:2},{id:3,numero:null}];
  sort.toggle('numero');expect(sort.ordered(rows,r=>r.numero).map(r=>r.id)).toEqual([2,1,3]);
  sort.toggle('numero');expect(sort.ordered(rows,r=>r.numero).map(r=>r.id)).toEqual([1,2,3]);expect(rows.map(r=>r.id)).toEqual([1,2,3]);
  sort.toggle('nome');expect(sort.direction()).toBe('ASC');
 });
 it('usa ano numérico e letra como critérios compostos, inclusive em ordem descendente',()=>{
  const sort=new ListSort(),rows=[{id:1,ano:2,letra:'A'},{id:2,ano:1,letra:'B'},{id:3,ano:1,letra:'A'}];
  sort.toggle('turma');expect(sort.ordered(rows,r=>[r.ano,r.letra]).map(r=>r.id)).toEqual([3,2,1]);
  sort.toggle('turma');expect(sort.ordered(rows,r=>[r.ano,r.letra]).map(r=>r.id)).toEqual([1,2,3]);
 });
 it('mantém empates estáveis por ID e inclui a ordenação na próxima pesquisa',()=>{
  const sort=new ListSort();sort.toggle('nome');expect(sort.ordered([{id:2,nome:'Ana'},{id:1,nome:'Ana'}],r=>r.nome).map(r=>r.id)).toEqual([1,2]);
  expect(sort.query()).toBe('&ordenacao=nome&direcao=ASC');sort.reset();expect(sort.query()).toBe('');
 });
});

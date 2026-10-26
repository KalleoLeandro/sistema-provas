import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { vi } from 'vitest';
import { provideDesignSystem } from '../../../design-system/providers';
import { ApiService } from '../../../services/api.service';
import { ListarCorrecoesComponent } from './listar';
describe('Lista de correções',()=>{const get=vi.fn();beforeEach(()=>{get.mockReset().mockImplementation((path:string)=>Promise.resolve(path==='/professor/dashboard'?{materias:[{turmas:[{id:5,ensino:'F',ano:1,letra:'A'}]}]}:{itens:[],total:0,pagina:1,tamanho:15}));TestBed.configureTestingModule({providers:[provideRouter([]),provideHttpClient(),provideDesignSystem(),{provide:ApiService,useValue:{get}}]});});it('envia filtros e paginação mantendo a consulta restrita ao professor da sessão',async()=>{const f=TestBed.createComponent(ListarCorrecoesComponent);await f.whenStable();const c=f.componentInstance;c.turmaId=5;c.situacao='pendentes';c.changeSize(25);await f.whenStable();expect(get).toHaveBeenLastCalledWith('/professor/correcoes?pagina=1&tamanho=25&situacao=pendentes&turmaId=5');});});

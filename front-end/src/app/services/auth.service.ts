import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Perfil, Usuario } from '../models/usuario';

/**
 * Mantém em memória os dados públicos do usuário autenticado. O cookie HttpOnly
 * e a validade da sessão são controlados pelo servidor; credenciais e tokens
 * de autenticação não são persistidos em localStorage ou sessionStorage.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly usuario = signal<Usuario | null>(null);

  /**
   * Consulta e restaura a identidade da sessão atual.
   *
   * @returns Usuário autenticado ou null quando a API confirma ausência de autenticação.
   * @throws Erro da consulta quando não é possível confirmar a sessão.
   */
  async restore(): Promise<Usuario | null> {
    /*
     * O servidor é a referência da autenticação. Os dados locais só são
     * atualizados quando a consulta com o cookie é concluída com sucesso.
     */
    try {
      const usuario = await firstValueFrom(this.http.get<Usuario>('/api/login/me'));
      this.usuario.set(usuario);
      return usuario;
    } catch (error) {
      /*
       * Somente o 401 confirma que o acesso terminou. Erros de recurso ou
       * conexão preservam o usuário local e seguem ao guard para nova tentativa.
       */
      if (error instanceof HttpErrorResponse && error.status === 401) {
        this.usuario.set(null);
        return null;
      }
      throw error;
    }
  }

  /**
   * Obtém o token de proteção das operações autenticadas por cookie.
   *
   * @returns Token CSRF e nome do header exigido pela API.
   */
  private async csrf() {
    /*
     * A proteção CSRF precisa corresponder à sessão atual. Buscar o token
     * não substitui o login nem solicita uma renovação do prazo de acesso.
     */
    return firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/login/csrf'));
  }

  /**
   * Solicita autenticação e registra o usuário confirmado pela API.
   *
   * @param login Identificação informada no formulário.
   * @param senha Senha usada somente na tentativa atual.
   * @param perfil Perfil selecionado para acesso.
   * @returns Conclusão da autenticação.
   */
  async login(login: string, senha: string, perfil: Perfil): Promise<void> {
    /*
     * O envio de credenciais exige proteção CSRF. O cookie é administrado
     * pelo navegador e a senha não é guardada no estado desta service.
     */
    const csrf = await this.csrf();
    const usuario = await firstValueFrom(this.http.post<Usuario>('/api/login/validar-login',
      (perfil==='aluno'?{login,perfil}:{login,senha,perfil}), { headers: { [csrf.headerName]: csrf.token } }));
    /*
     * A interface só passa a considerar o usuário autenticado depois da
     * confirmação do servidor; uma tentativa recusada não estabelece sessão.
     */
    this.usuario.set(usuario);
  }

  /**
   * Solicita o encerramento da sessão atual.
   *
   * @returns Conclusão da saída ou da confirmação de que não há sessão.
   * @throws Erro da API quando não é possível concluir a saída.
   */
  async logout(): Promise<void> {
    /*
     * Solicita a invalidação no servidor antes de limpar a identidade local,
     * evitando apresentar uma saída concluída enquanto o cookie segue válido.
     */
    try {
      const csrf = await this.csrf();
      await firstValueFrom(this.http.post('/api/login/logout', {},
        { headers: { [csrf.headerName]: csrf.token } }));
    } catch (error) {
      /*
       * O 401 permite concluir a saída porque a autenticação já terminou.
       * Nas demais falhas, o estado é mantido para permitir outra tentativa.
       */
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) throw error;
    }
    this.usuario.set(null);
  }
}

import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

/**
 * Centraliza a comunicação das páginas com a API da mesma origem. Os erros do
 * servidor são preservados para que cada fluxo apresente sua mensagem de negócio;
 * uma falha de recurso nesta camada não é convertida em encerramento de sessão.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  /**
   * Consulta um recurso da API.
   *
   * @param path Caminho do recurso sem o prefixo /api.
   * @returns Dados retornados pela consulta.
   */
  get<T>(path: string): Promise<T> {
    /*
     * A leitura utiliza a sessão enviada pelo navegador, sem exigir CSRF.
     * Erros permanecem disponíveis ao fluxo chamador, sem provocar logout aqui.
     */
    return firstValueFrom(this.http.get<T>('/api' + path));
  }

  /**
   * Solicita a exclusão de um recurso.
   *
   * @param path Caminho do recurso a excluir.
   * @returns Conclusão da solicitação.
   */
  async delete(path: string): Promise<void> {
    /*
     * A confirmação de exclusão pertence ao fluxo chamador. Esta camada
     * exige o token CSRF atual e preserva a recusa da API para apresentação
     * ao usuário, inclusive quando o recurso está vinculado ou não existe.
     */
    const csrf = await firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/login/csrf'));
    await firstValueFrom(this.http.delete<void>('/api' + path, { headers: { [csrf.headerName]: csrf.token } }));
  }

  /**
   * Envia uma atualização parcial para a API.
   *
   * @param path Caminho do recurso.
   * @param body Dados da atualização parcial.
   * @returns Resposta da operação.
   */
  async patch<T>(path: string, body: unknown): Promise<T> {
    /*
     * Alterações parciais, como a troca de status, usam a sessão atual e
     * sua proteção CSRF. Validar a mudança permitida é responsabilidade da API.
     */
    const csrf = await firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/login/csrf'));
    return firstValueFrom(this.http.patch<T>('/api' + path, body, { headers: { [csrf.headerName]: csrf.token } }));
  }

  /**
   * Envia os dados de atualização de um recurso.
   *
   * @param path Caminho do recurso.
   * @param body Dados enviados para atualização.
   * @returns Resposta da operação.
   */
  async put<T>(path: string, body: unknown): Promise<T> {
    /*
     * A gravação só é enviada após obter a proteção CSRF da sessão atual.
     * Conflitos e erros de validação são propagados para que a página
     * preserve o formulário e apresente a orientação fornecida pelo servidor.
     */
    const csrf = await firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/login/csrf'));
    return firstValueFrom(this.http.put<T>('/api' + path, body, { headers: { [csrf.headerName]: csrf.token } }));
  }

  /**
   * Envia uma operação com corpo para a API.
   *
   * @param path Caminho da operação.
   * @param body Dados enviados na solicitação.
   * @returns Resposta da operação.
   */
  async post<T>(path: string, body: unknown): Promise<T> {
    /*
     * Operações por POST também exigem proteção CSRF por usarem cookie.
     * A página recebe sucesso ou erro da API e decide o próximo passo,
     * como continuar o cadastro ou retornar à listagem.
     */
    const csrf = await firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/login/csrf'));
    return firstValueFrom(this.http.post<T>('/api' + path, body, { headers: { [csrf.headerName]: csrf.token } }));
  }
}

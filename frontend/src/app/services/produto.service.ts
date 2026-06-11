import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produto, ProdutoRequest } from '../models/produto.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarDisponiveis(): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.API}/produtos`);
  }

  criarProduto(produto: ProdutoRequest): Observable<Produto> {
    return this.http.post<Produto>(`${this.API}/produtos`, produto);
  }

  excluirProduto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/produtos/${id}`);
  }

  venderProduto(id: number): Observable<Produto> {
    return this.http.patch<Produto>(`${this.API}/produtos/${id}/vender`, {});
  }

  listarPorVendedor(email: string): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.API}/produtos/vendedor/email/${email}`);
  }

  uploadImagem(produtoId: number, arquivo: File): Observable<string> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post(`${this.API}/produtos/${produtoId}/imagem`, formData, {
      responseType: 'text',
    });
  }

  atualizar(id: number, produto: ProdutoRequest): Observable<Produto> {
    return this.http.put<Produto>(`${this.API}/produtos/${id}`, produto);
  }
}

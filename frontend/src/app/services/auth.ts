import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UsuarioLogado {
  email: string;
  admin: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  constructor() {}

  registrarVendedor(vendedor: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/usuarios`, vendedor);
  }

  buscarUsuarioLogado(): Observable<UsuarioLogado> {
    return this.http.get<UsuarioLogado>(`${this.apiUrl}/auth/me`);
  }

  login(credenciais: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credenciais).pipe(
      tap((resposta) => {
        if (resposta && resposta.token) {
          localStorage.setItem('token_desapego', resposta.token);
        }
      }),
    );
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    if (this.tokenExpirado(token)) {
      this.logout();
      return false;
    }

    return true;
  }

  getToken(): string | null {
    return localStorage.getItem('token_desapego');
  }

  logout(): void {
    localStorage.removeItem('token_desapego');
  }

  getUsuarioEmail(): string | null {
    const token = this.getToken();
    if (!token || this.tokenExpirado(token)) {
      this.logout();
      return null;
    }

    try {
      const payload = this.payloadToken(token);
      return payload.sub ?? null;
    } catch {
      return null;
    }
  }

  private tokenExpirado(token: string): boolean {
    try {
      const payload = this.payloadToken(token);
      if (!payload.exp) return false;

      return Date.now() >= payload.exp * 1000;
    } catch {
      return true;
    }
  }

  private payloadToken(token: string): any {
    const payloadBase64 = token.split('.')[1];
    const payloadNormalizado = payloadBase64
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(Math.ceil(payloadBase64.length / 4) * 4, '=');

    return JSON.parse(atob(payloadNormalizado));
  }
}

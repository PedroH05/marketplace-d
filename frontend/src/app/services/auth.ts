import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

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
    return !!localStorage.getItem('token_desapego');
  }

  getToken(): string | null {
    return localStorage.getItem('token_desapego');
  }

  logout(): void {
    localStorage.removeItem('token_desapego');
  }

  getUsuarioEmail(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub ?? null;
    } catch {
      return null;
    }
  }

  isAdmin(): boolean {
    const email = this.getUsuarioEmail();
    if (!email) return false;

    return environment.adminEmails.includes(email.trim().toLowerCase());
  }
}

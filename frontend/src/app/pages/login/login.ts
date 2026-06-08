import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
  });

  loading = signal<boolean>(false);
  erro = signal<string | null>(null);
  mostrarSenha = signal<boolean>(false);

  get emailInvalido(): boolean {
    const campo = this.form.get('email');
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

  get senhaInvalida(): boolean {
    const campo = this.form.get('senha');
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

  toggleSenha(): void {
    this.mostrarSenha.update((state) => !state);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.erro.set(null);

    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.loading.set(false);

        this.router.navigate(['/vitrine']).then(() => {
          window.location.reload();
        });
      },
      error: (err: any) => {
        console.error('Erro ao fazer login:', err);
        this.loading.set(false);
        this.erro.set('E-mail ou senha incorretos, ou o servidor está inacessível.');
      },
    });
  }
}

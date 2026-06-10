import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro.html',
  styleUrls: ['./cadastro.css'],
})
export class CadastroComponent {
  private fb = inject(FormBuilder);
  public authService = inject(AuthService);
  private router = inject(Router);

  loading = signal<boolean>(false);
  erro = signal<string | null>(null);
  sucesso = signal<boolean>(false);
  mostrarSenha = signal<boolean>(false);

  form: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    nick: ['', [Validators.required, Validators.minLength(3), Validators.pattern('^[a-zA-Z0-9_]+$')]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
  });

  get nomeInvalido(): boolean {
    const campo = this.form.get('nome');
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

  get nickInvalido(): boolean {
    const campo = this.form.get('nick');
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

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

    this.authService.registrarVendedor(this.form.value as any).subscribe({
      next: () => {
        this.loading.set(false);
        this.sucesso.set(true);
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        console.error('Erro ao cadastrar:', err);
        this.loading.set(false);
        this.erro.set('Erro ao criar conta. Esse e-mail ou nick já pode estar em uso.');
      },
    });
  }
}

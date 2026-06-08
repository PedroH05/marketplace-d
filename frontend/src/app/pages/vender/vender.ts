import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProdutoService } from '../../services/produto.service';
import { environment } from '../../../environments/environment';

type ImagemSelecionada = {
  file: File;
  preview: string;
};

@Component({
  selector: 'app-vender',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './vender.html',
  styleUrls: ['./vender.css'],
})
export class VenderComponent {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);
  private router = inject(Router);

  private readonly limiteImagens = 8;
  private readonly tamanhoMaximoImagem = 10 * 1024 * 1024;

  loading = signal<boolean>(false);
  erro = signal<string | null>(null);
  sucesso = signal<boolean>(false);
  imagemPreviews = signal<string[]>([]);

  imagensSelecionadas: ImagemSelecionada[] = [];

  form: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    preco: ['', [Validators.required, Validators.min(0.01)]],
    descricao: ['', [Validators.required, Validators.maxLength(500)]],
  });

  get nomeInvalido(): boolean {
    const c = this.form.get('nome');
    return !!(c && c.invalid && (c.dirty || c.touched));
  }

  get precoInvalido(): boolean {
    const c = this.form.get('preco');
    return !!(c && c.invalid && (c.dirty || c.touched));
  }

  get descricaoInvalida(): boolean {
    const c = this.form.get('descricao');
    return !!(c && c.invalid && (c.dirty || c.touched));
  }

  onImagemSelecionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivos = Array.from(input.files ?? []);
    if (!arquivos.length) return;

    if (this.imagensSelecionadas.length + arquivos.length > this.limiteImagens) {
      this.erro.set(`Você pode adicionar no máximo ${this.limiteImagens} imagens.`);
      input.value = '';
      return;
    }

    const arquivoGrande = arquivos.find((arquivo) => arquivo.size > this.tamanhoMaximoImagem);
    if (arquivoGrande) {
      this.erro.set('Cada imagem deve ter no máximo 10MB.');
      input.value = '';
      return;
    }

    arquivos.forEach((arquivo) => {
      const reader = new FileReader();
      reader.onload = () => {
        this.imagensSelecionadas.push({
          file: arquivo,
          preview: reader.result as string,
        });
        this.atualizarPreviews();
      };
      reader.readAsDataURL(arquivo);
    });

    input.value = '';
    this.erro.set(null);
  }

  removerImagem(index: number): void {
    this.imagensSelecionadas.splice(index, 1);
    this.atualizarPreviews();
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.erro.set(null);

    try {
      const imagemUrls = await Promise.all(
        this.imagensSelecionadas.map((imagem) => this.uploadImagemCloudinary(imagem.file)),
      );
      this.salvarNoJava(imagemUrls);
    } catch (error) {
      console.error('Erro no Cloudinary:', error);
      this.loading.set(false);
      this.erro.set('Erro ao subir uma ou mais imagens para a nuvem.');
    }
  }

  private atualizarPreviews(): void {
    this.imagemPreviews.set(this.imagensSelecionadas.map((imagem) => imagem.preview));
  }

  private async uploadImagemCloudinary(arquivo: File): Promise<string> {
    if (!environment.cloudinaryCloudName || !environment.cloudinaryUploadPreset) {
      throw new Error('Cloudinary não configurado');
    }

    const formData = new FormData();
    formData.append('file', arquivo);
    formData.append('upload_preset', environment.cloudinaryUploadPreset);

    const response = await fetch(`https://api.cloudinary.com/v1_1/${environment.cloudinaryCloudName}/image/upload`, {
      method: 'POST',
      body: formData,
    });

    const data = await response.json();

    if (!response.ok || data.error) {
      throw new Error(data.error?.message || 'Falha no upload da imagem');
    }

    const linkDaFoto = data.secure_url as string | undefined;
    if (!linkDaFoto) {
      throw new Error('Cloudinary não retornou secure_url');
    }

    return linkDaFoto;
  }

  private salvarNoJava(imagemUrls: string[]): void {
    const novoProduto = {
      nome: this.form.value.nome,
      preco: parseFloat(this.form.value.preco),
      descricao: this.form.value.descricao,
      imagemUrl: imagemUrls[0] ?? null,
      imagemUrls,
    };

    this.produtoService.criarProduto(novoProduto).subscribe({
      next: () => {
        this.finalizarCriacao();
      },
      error: (err) => {
        console.error('Erro ao criar produto no Java:', err);
        this.loading.set(false);
        this.erro.set('Erro ao salvar o produto na base de dados.');
      },
    });
  }

  private finalizarCriacao(): void {
    this.loading.set(false);
    this.sucesso.set(true);
    setTimeout(() => this.router.navigate(['/vitrine']), 2000);
  }
}

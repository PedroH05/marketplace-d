import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProdutoService } from '../../services/produto.service';
import { AuthService } from '../../services/auth';
import { Produto, ProdutoRequest } from '../../models/produto.model';
import { environment } from '../../../environments/environment';
import {
  formatPrecoBrasileiro,
  parsePrecoBrasileiro,
  precoBrasileiroValidator,
} from '../../utils/preco-br';

type ImagemSelecionada = {
  file: File;
  preview: string;
};

@Component({
  selector: 'app-meus-anuncios',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './meus-anuncios.html',
  styleUrl: './meus-anuncios.css',
})
export class MeusAnunciosComponent implements OnInit {
  private produtoService = inject(ProdutoService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  private readonly limiteImagens = 8;
  private readonly tamanhoMaximoImagem = 10 * 1024 * 1024;

  produtos = signal<Produto[]>([]);
  loading = signal<boolean>(true);
  erro = signal<string | null>(null);

  produtoEditando = signal<Produto | null>(null);
  salvando = signal<boolean>(false);
  erroEdicao = signal<string | null>(null);
  excluindoId = signal<number | null>(null);
  imagensEdicao = signal<string[]>([]);
  novasImagensPreview = signal<string[]>([]);
  indicesImagem = signal<Record<number, number>>({});

  novasImagensEdicao: ImagemSelecionada[] = [];

  formEdicao: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    preco: ['', [Validators.required, precoBrasileiroValidator]],
    descricao: ['', [Validators.minLength(10)]],
  });

  ngOnInit() {
    this.carregarMeusAnuncios();
  }

  carregarMeusAnuncios() {
    const email = this.authService.getUsuarioEmail();
    if (!email) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading.set(true);
    this.erro.set(null);

    this.produtoService.listarPorVendedor(email).subscribe({
      next: (lista) => {
        this.produtos.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar seus anúncios.');
        this.loading.set(false);
      },
    });
  }

  abrirEdicao(produto: Produto, event: Event) {
    event.stopPropagation();
    this.produtoEditando.set(produto);
    this.erroEdicao.set(null);
    this.imagensEdicao.set(this.imagensDoProduto(produto));
    this.novasImagensEdicao = [];
    this.novasImagensPreview.set([]);
    this.formEdicao.patchValue({
      nome: produto.nome,
      preco: formatPrecoBrasileiro(produto.preco),
      descricao: produto.descricao || '',
    });
  }

  fecharEdicao() {
    this.produtoEditando.set(null);
    this.formEdicao.reset();
    this.imagensEdicao.set([]);
    this.novasImagensEdicao = [];
    this.novasImagensPreview.set([]);
  }

  onNovasImagensSelecionadas(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivos = Array.from(input.files ?? []);
    if (!arquivos.length) return;

    const total = this.imagensEdicao().length + this.novasImagensEdicao.length + arquivos.length;
    if (total > this.limiteImagens) {
      this.erroEdicao.set(`O anúncio pode ter no máximo ${this.limiteImagens} imagens.`);
      input.value = '';
      return;
    }

    const arquivoGrande = arquivos.find((arquivo) => arquivo.size > this.tamanhoMaximoImagem);
    if (arquivoGrande) {
      this.erroEdicao.set('Cada imagem deve ter no máximo 10MB.');
      input.value = '';
      return;
    }

    arquivos.forEach((arquivo) => {
      const reader = new FileReader();
      reader.onload = () => {
        this.novasImagensEdicao.push({
          file: arquivo,
          preview: reader.result as string,
        });
        this.atualizarNovasPreviews();
      };
      reader.readAsDataURL(arquivo);
    });

    input.value = '';
    this.erroEdicao.set(null);
  }

  removerImagemExistente(index: number): void {
    this.imagensEdicao.update((imagens) => imagens.filter((_, i) => i !== index));
  }

  removerNovaImagem(index: number): void {
    this.novasImagensEdicao.splice(index, 1);
    this.atualizarNovasPreviews();
  }

  formatarPrecoEdicao(): void {
    const controle = this.formEdicao.get('preco');
    const precoFormatado = formatPrecoBrasileiro(controle?.value);

    if (precoFormatado) {
      controle?.setValue(precoFormatado, { emitEvent: false });
    }
  }

  async salvarEdicao(): Promise<void> {
    if (this.formEdicao.invalid) {
      this.formEdicao.markAllAsTouched();
      return;
    }

    const produto = this.produtoEditando();
    if (!produto) return;

    this.salvando.set(true);
    this.erroEdicao.set(null);

    try {
      const novasUrls = await Promise.all(
        this.novasImagensEdicao.map((imagem) => this.uploadImagemCloudinary(imagem.file)),
      );

      const imagemUrls = [...this.imagensEdicao(), ...novasUrls];
      const descricao = this.formEdicao.value.descricao?.trim();

      const payload: ProdutoRequest = {
        nome: this.formEdicao.value.nome.trim(),
        preco: parsePrecoBrasileiro(this.formEdicao.value.preco),
        descricao: descricao || undefined,
        imagemUrl: imagemUrls[0] ?? null,
        imagemUrls,
      };

      this.produtoService.atualizar(produto.id, payload).subscribe({
        next: () => {
          this.salvando.set(false);
          this.fecharEdicao();
          this.carregarMeusAnuncios();
        },
        error: (err) => {
          this.salvando.set(false);
          const mensagemValidacao = err.error?.erros?.[0] || err.error?.erro;
          this.erroEdicao.set(mensagemValidacao || 'Erro ao salvar. Tente novamente.');
        },
      });
    } catch (error) {
      console.error('Erro ao enviar imagens:', error);
      this.salvando.set(false);
      this.erroEdicao.set('Erro ao subir uma ou mais imagens.');
    }
  }

  excluirAnuncio(produto: Produto, event: Event) {
    event.stopPropagation();

    const confirmar = confirm(`Deseja excluir o anúncio "${produto.nome}"?`);
    if (!confirmar) return;

    this.excluindoId.set(produto.id);
    this.erro.set(null);

    this.produtoService.excluirProduto(produto.id).subscribe({
      next: () => {
        this.produtos.update((lista) => lista.filter((item) => item.id !== produto.id));
        this.excluindoId.set(null);
      },
      error: (err) => {
        this.excluindoId.set(null);
        this.erro.set(err.error?.erro || 'Não foi possível excluir o anúncio.');
      },
    });
  }

  imagensDoProduto(produto: Produto | null | undefined): string[] {
    if (!produto) return [];
    if (produto.imagemUrls?.length) return produto.imagemUrls;
    return produto.imagemUrl ? [produto.imagemUrl] : [];
  }

  imagemAtual(produto: Produto): string | null {
    const imagens = this.imagensDoProduto(produto);
    if (imagens.length === 0) return null;
    return imagens[this.indiceImagem(produto)] ?? imagens[0];
  }

  indiceImagem(produto: Produto): number {
    const imagens = this.imagensDoProduto(produto);
    if (imagens.length === 0) return 0;

    const atual = this.indicesImagem()[produto.id] ?? 0;
    return Math.min(atual, imagens.length - 1);
  }

  mudarImagem(produto: Produto, direcao: number, event: Event) {
    event.stopPropagation();

    const imagens = this.imagensDoProduto(produto);
    if (imagens.length <= 1) return;

    const atual = this.indiceImagem(produto);
    const proximo = (atual + direcao + imagens.length) % imagens.length;
    this.indicesImagem.update((indices) => ({
      ...indices,
      [produto.id]: proximo,
    }));
  }

  irParaCriar() {
    this.router.navigate(['/vender']);
  }

  corPlaceholder(id: number): string {
    const cores = ['#fce4ec', '#e3f2fd', '#e8f5e9', '#fff3e0', '#f3e5f5', '#e0f7fa'];
    return cores[id % cores.length];
  }

  inicialProduto(nome: string = ''): string {
    return nome.charAt(0).toUpperCase();
  }

  formatarPreco(preco: number): string {
    return preco?.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) ?? '';
  }

  private atualizarNovasPreviews(): void {
    this.novasImagensPreview.set(this.novasImagensEdicao.map((imagem) => imagem.preview));
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
}

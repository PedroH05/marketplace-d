import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ProdutoService } from '../../services/produto.service';
import { AuthService } from '../../services/auth';
import { Produto } from '../../models/produto.model';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-vitrine',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vitrine.html',
  styleUrls: ['./vitrine.css'],
})
export class VitrineComponent implements OnInit {
  private produtoService = inject(ProdutoService);
  private chatService = inject(ChatService);
  public authService = inject(AuthService);
  private router = inject(Router);

  produtos = signal<Produto[]>([]);
  loading = signal<boolean>(false);
  erro = signal<string | null>(null);
  produtoSelecionado = signal<Produto | null>(null);
  indicesImagem = signal<Record<number, number>>({});
  abrindoChat = signal<boolean>(false);
  erroContato = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarProdutos();
  }

  carregarProdutos(): void {
    this.loading.set(true);
    this.erro.set(null);

    this.produtoService.listarDisponiveis().subscribe({
      next: (dados) => {
        this.produtos.set(dados || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao buscar produtos:', err);
        this.erro.set('Não foi possível carregar os produtos. Tente novamente mais tarde.');
        this.loading.set(false);
      },
    });
  }

  irParaVender(): void {
    this.router.navigate(['/vender']);
  }

  logout(): void {
    this.chatService.desconectar();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  abrirProduto(produto: Produto): void {
    this.produtoSelecionado.set(produto);
    this.erroContato.set(null);
  }

  fecharModal(): void {
    this.produtoSelecionado.set(null);
    this.erroContato.set(null);
  }

  entrarEmContato(produto: Produto | null): void {
    if (!produto || this.abrindoChat()) return;

    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.abrindoChat.set(true);
    this.erroContato.set(null);

    this.chatService.iniciarChat(produto.id).subscribe({
      next: (chatId) => {
        this.abrindoChat.set(false);
        this.fecharModal();
        this.router.navigate(['/chats', chatId]);
      },
      error: (err) => {
        this.abrindoChat.set(false);
        this.erroContato.set(err.error?.erro || 'Não foi possível iniciar a conversa.');
      },
    });
  }

  imagensDoProduto(produto: Produto | null | undefined): string[] {
    if (!produto) return [];
    if (produto.imagemUrls?.length) return produto.imagemUrls;
    return produto.imagemUrl ? [produto.imagemUrl] : [];
  }

  imagemAtual(produto: Produto | null | undefined): string | null {
    const imagens = this.imagensDoProduto(produto);
    if (!produto || imagens.length === 0) return null;
    return imagens[this.indiceImagem(produto)] ?? imagens[0];
  }

  indiceImagem(produto: Produto): number {
    const imagens = this.imagensDoProduto(produto);
    if (imagens.length === 0) return 0;

    const atual = this.indicesImagem()[produto.id] ?? 0;
    return Math.min(atual, imagens.length - 1);
  }

  mudarImagem(produto: Produto, direcao: number, event?: Event): void {
    event?.stopPropagation();

    const imagens = this.imagensDoProduto(produto);
    if (imagens.length <= 1) return;

    const atual = this.indiceImagem(produto);
    const proximo = (atual + direcao + imagens.length) % imagens.length;
    this.indicesImagem.update((indices) => ({
      ...indices,
      [produto.id]: proximo,
    }));
  }

  formatarPreco(preco: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(preco);
  }

  inicialProduto(nome: string = ''): string {
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }

  corPlaceholder(id: number): string {
    const cores = ['#e3f2fd', '#fce4ec', '#f3e5f5', '#efebe9', '#e8f5e9', '#fff3e0'];
    return cores[id % cores.length] || '#e0e0e0';
  }
}

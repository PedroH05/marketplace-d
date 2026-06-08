import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, effect, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ChatMensagem, ChatResumo } from '../../models/chat.model';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-chat-conversa',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './chat-conversa.html',
  styleUrls: ['./chat-conversa.css'],
})
export class ChatConversaComponent implements OnInit, OnDestroy {
  @ViewChild('mensagensContainer') private mensagensContainer?: ElementRef<HTMLDivElement>;

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chatService = inject(ChatService);
  private websocketSub?: Subscription;

  chatId = 0;
  resumo = signal<ChatResumo | null>(null);
  mensagens = signal<ChatMensagem[]>([]);
  loading = signal<boolean>(true);
  enviando = signal<boolean>(false);
  erro = signal<string | null>(null);

  mensagemControl = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(1000)],
  });

  constructor() {
    effect(() => {
      if (this.mensagens().length > 0) {
        window.setTimeout(() => this.rolarParaBaixo(), 50);
      }
    });
  }

  ngOnInit(): void {
    this.chatId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.chatId || Number.isNaN(this.chatId)) {
      this.router.navigate(['/chats']);
      return;
    }

    this.carregarDadosIniciais();
    this.conectarNoWebSocket();
  }

  ngOnDestroy(): void {
    this.websocketSub?.unsubscribe();
  }

  carregarDadosIniciais(): void {
    this.loading.set(true);
    this.erro.set(null);

    this.chatService.buscarHistorico(this.chatId).subscribe({
      next: (historico) => {
        this.mensagens.set(historico || []);
        this.loading.set(false);
        this.carregarResumo();
      },
      error: (err) => {
        this.erro.set(err.error?.erro || 'Não foi possível carregar o histórico.');
        this.loading.set(false);
      },
    });
  }

  conectarNoWebSocket(): void {
    this.websocketSub = this.chatService.escutarChat(this.chatId).subscribe({
      next: (mensagemNova) => {
        this.adicionarMensagem(mensagemNova);
        this.carregarResumo();
      },
      error: (err) => console.error('Erro no WebSocket do chat:', err),
    });
  }

  enviarMensagem(valorDigitado?: string): void {
    const texto = (valorDigitado ?? this.mensagemControl.value).trim();

    if (!texto || this.enviando()) {
      this.mensagemControl.markAsTouched();
      this.erro.set('Digite uma mensagem antes de enviar.');
      return;
    }

    if (texto.length > 1000) {
      this.mensagemControl.markAsTouched();
      this.erro.set('A mensagem deve ter no máximo 1000 caracteres.');
      return;
    }

    this.enviando.set(true);
    this.erro.set(null);

    this.chatService.enviarMensagem(this.chatId, texto).subscribe({
      next: (mensagem) => {
        this.adicionarMensagem(mensagem);
        this.mensagemControl.reset('');
        this.enviando.set(false);
        this.carregarResumo();
      },
      error: (err) => {
        console.error('[Chat] erro ao enviar mensagem', err);
        this.erro.set(err.error?.erro || 'Não foi possível enviar a mensagem.');
        this.enviando.set(false);
      },
    });
  }

  ehMinhaMensagem(mensagem: ChatMensagem): boolean {
    return mensagem.remetenteId === this.resumo()?.usuarioLogadoId;
  }

  formatarHora(dataStr: string): string {
    if (!dataStr) return '';
    return new Intl.DateTimeFormat('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(dataStr));
  }

  inicial(nome?: string): string {
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }

  private carregarResumo(): void {
    this.chatService.listarChats().subscribe({
      next: (lista) => {
        const conversaAtual = lista.find((chat) => chat.id === this.chatId) ?? null;
        this.resumo.set(conversaAtual);
      },
      error: () => {
        this.erro.set('Não foi possível carregar os dados da conversa.');
      },
    });
  }

  private adicionarMensagem(mensagem: ChatMensagem): void {
    this.mensagens.update((lista) => {
      if (lista.some((item) => item.id === mensagem.id)) {
        return lista;
      }

      return [...lista, mensagem];
    });
  }

  private rolarParaBaixo(): void {
    const container = this.mensagensContainer?.nativeElement;
    if (container) {
      container.scrollTop = container.scrollHeight;
    }
  }
}

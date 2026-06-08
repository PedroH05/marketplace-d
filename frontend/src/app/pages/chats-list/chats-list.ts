import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ChatResumo } from '../../models/chat.model';
import { ChatService } from '../../services/chat.service';

@Component({
  selector: 'app-chats-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './chats-list.html',
  styleUrl: './chats-list.css',
})
export class ChatsListComponent implements OnInit {
  private chatService = inject(ChatService);
  private router = inject(Router);

  chats = signal<ChatResumo[]>([]);
  loading = signal<boolean>(true);
  erro = signal<string | null>(null);
  removendoId = signal<number | null>(null);

  ngOnInit(): void {
    this.carregarChats();
  }

  carregarChats(): void {
    this.loading.set(true);
    this.erro.set(null);

    this.chatService.listarChats().subscribe({
      next: (chats) => {
        this.chats.set(chats || []);
        this.loading.set(false);
      },
      error: (err) => {
        this.erro.set(err.error?.erro || 'Não foi possível carregar suas conversas.');
        this.loading.set(false);
      },
    });
  }

  abrirChat(chat: ChatResumo): void {
    this.router.navigate(['/chats', chat.id]);
  }

  removerChat(chat: ChatResumo, event: MouseEvent): void {
    event.stopPropagation();

    const confirmou = window.confirm('Remover esta conversa da sua lista? Novas mensagens farão ela aparecer novamente.');
    if (!confirmou) return;

    this.removendoId.set(chat.id);
    this.erro.set(null);

    this.chatService.removerChatDaLista(chat.id).subscribe({
      next: () => {
        this.chats.update((lista) => lista.filter((item) => item.id !== chat.id));
        this.removendoId.set(null);
      },
      error: (err) => {
        this.erro.set(err.error?.erro || 'Não foi possível remover a conversa.');
        this.removendoId.set(null);
      },
    });
  }

  ultimaMensagem(chat: ChatResumo): string {
    if (!chat.ultimaMensagem) return 'Conversa iniciada';
    const prefixo = chat.ultimaMensagemRemetenteId === chat.usuarioLogadoId ? 'Você: ' : '';
    return `${prefixo}${chat.ultimaMensagem}`;
  }

  formatarData(data?: string | null): string {
    if (!data) return '';

    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(data));
  }

  inicial(nome: string = ''): string {
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }
}

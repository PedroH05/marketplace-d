import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { RxStomp } from '@stomp/rx-stomp';
import { ChatMensagem, ChatResumo } from '../models/chat.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiUrl}/api/chats`;
  private readonly WS = `${environment.wsUrl}/ws-desapego`;
  private rxStomp = new RxStomp();
  private stompAtivado = false;

  iniciarChat(produtoId: number): Observable<number> {
    return this.http.post<number>(this.API, { produtoId });
  }

  listarChats(): Observable<ChatResumo[]> {
    return this.http.get<ChatResumo[]>(this.API);
  }

  buscarHistorico(chatId: number): Observable<ChatMensagem[]> {
    return this.http.get<ChatMensagem[]>(`${this.API}/${chatId}/historico`);
  }

  enviarMensagem(chatId: number, mensagem: string): Observable<ChatMensagem> {
    return this.http.post<ChatMensagem>(`${this.API}/${chatId}/mensagens`, { mensagem });
  }

  removerChatDaLista(chatId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${chatId}`);
  }

  escutarChat(chatId: number): Observable<ChatMensagem> {
    this.garantirConexao();

    return this.rxStomp.watch(`/topic/chat/${chatId}`).pipe(
      map((message) => JSON.parse(message.body) as ChatMensagem),
    );
  }

  desconectar(): void {
    this.rxStomp.deactivate();
    this.stompAtivado = false;
  }

  private garantirConexao(): void {
    if (this.stompAtivado) return;

    const token = localStorage.getItem('token_desapego');
    if (!token) return;

    this.rxStomp.configure({
      brokerURL: this.WS,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
    });

    this.rxStomp.activate();
    this.stompAtivado = true;
  }
}

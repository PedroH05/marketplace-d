export interface ChatResumo {
  id: number;
  usuarioLogadoId: number;
  produtoId: number;
  produtoNome: string;
  produtoImagemUrl?: string | null;
  produtoStatus: string;
  outroUsuarioId: number;
  outroUsuarioNome: string;
  outroUsuarioNick: string;
  ultimaMensagemRemetenteId?: number | null;
  ultimaMensagem?: string | null;
  dataUltimaMensagem?: string | null;
}

export interface ChatMensagem {
  id: number;
  remetenteId: number;
  remetenteNome: string;
  mensagem: string;
  dataEnvio: string;
}

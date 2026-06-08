export interface Produto {
  id: number;
  nome: string;
  preco: number;
  descricao: string;
  status: 'DISPONIVEL' | 'VENDIDO';
  imagemUrl?: string | null;
  imagemUrls?: string[];
  vendedor: {
    id: number;
    nome: string;
    nick: string;
  };
}

export interface ProdutoRequest {
  nome: string;
  preco: number;
  descricao?: string;
  imagemUrl?: string | null;
  imagemUrls?: string[];
}

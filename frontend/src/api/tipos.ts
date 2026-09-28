export type StatusConcurso = 'ABERTO' | 'PREVISTO' | 'ENCERRADO'
export type OrigemTi = 'LISTAGEM' | 'MCP' | 'DETALHE'
export type StatusColeta = 'EM_ANDAMENTO' | 'SUCESSO' | 'FALHA'

export interface Concurso {
  id: number
  urlOrigem: string
  titulo: string | null
  orgao: string
  cargo: string | null
  cargos: string[]
  cargosTi: string[]
  uf: string | null
  nacional: boolean
  escolaridade: string | null
  vagas: number | null
  cadastroReserva: boolean
  salarioMin: number | null
  salarioMax: number | null
  trechoRemuneracao: string | null
  trechoTaxa: string | null
  taxaInscricao: number | null
  banca: string | null
  status: StatusConcurso
  inicioInscricao: string | null
  fimInscricao: string | null
  dataProva: string | null
  origemTi: OrigemTi
  primeiraVezVistoEm: string
  atualizadoEm: string
  novo: boolean
  diasRestantes: number | null
}

export interface Pagina<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface UltimaColeta {
  id: number
  status: StatusColeta
  iniciadaEm: string
  finalizadaEm: string | null
  novos: number
  atualizados: number
  encerrados: number
}

export interface Resumo {
  abertos: number
  previstos: number
  encerrandoEm7Dias: number
  novosNaSemana: number
  ultimaColeta: UltimaColeta | null
}

export interface TotalPorUf {
  uf: string
  total: number
}

export interface TotalPorBanca {
  banca: string
  total: number
}

export interface TotalPorFaixa {
  faixa: string
  total: number
}

export interface Coleta {
  id: number
  status: StatusColeta
  origem: 'AGENDADA' | 'MANUAL'
  iniciadaEm: string
  finalizadaEm: string | null
  totalListagem: number
  novos: number
  atualizados: number
  encerrados: number
  descartados: number
  detalhesBaixados: number
  pendentes: number
  avisos: string[]
  mensagemErro: string | null
}

export interface SyncIniciado {
  coletaId: number
}

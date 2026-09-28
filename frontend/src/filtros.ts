import type { StatusConcurso } from './api/tipos'
import { NACIONAL, ORDENACOES, PISOS_SALARIO, STATUS, UFS, type Ordenacao } from './dominio'

export interface Filtros {
  q: string
  uf: string | null
  status: StatusConcurso
  banca: string | null
  salarioMin: number | null
  sort: Ordenacao
  page: number
}

export type MudancaFiltros = Partial<Omit<Filtros, 'page'>>

export const TAMANHO_PAGINA = 20
export const PAGINA_MAXIMA = 10_000

export const FILTROS_PADRAO: Filtros = {
  q: '',
  uf: null,
  status: 'ABERTO',
  banca: null,
  salarioMin: null,
  sort: 'fimInscricao,asc',
  page: 0,
}

export function lerFiltros(busca: string): Filtros {
  const params = new URLSearchParams(busca)
  const uf = params.get('uf')?.trim().toUpperCase() ?? null
  const status = params.get('status')
  const salarioMin = Number(params.get('salarioMin'))
  const sort = params.get('sort')
  const page = Number(params.get('page'))
  return {
    q: params.get('q')?.trim() ?? '',
    uf: uf && (uf === NACIONAL || (UFS as readonly string[]).includes(uf)) ? uf : null,
    status: STATUS.some((item) => item.valor === status) ? (status as StatusConcurso) : 'ABERTO',
    banca: params.get('banca')?.trim() || null,
    salarioMin: PISOS_SALARIO.includes(salarioMin) ? salarioMin : null,
    sort: ORDENACOES.some((item) => item.valor === sort) ? (sort as Ordenacao) : 'fimInscricao,asc',
    page: Number.isInteger(page) && page > 0 && page <= PAGINA_MAXIMA ? page : 0,
  }
}

export function escreverFiltros(filtros: Filtros): string {
  const params = new URLSearchParams()
  if (filtros.q) params.set('q', filtros.q)
  if (filtros.uf) params.set('uf', filtros.uf)
  if (filtros.status !== FILTROS_PADRAO.status) params.set('status', filtros.status)
  if (filtros.banca) params.set('banca', filtros.banca)
  if (filtros.salarioMin != null) params.set('salarioMin', String(filtros.salarioMin))
  if (filtros.sort !== FILTROS_PADRAO.sort) params.set('sort', filtros.sort)
  if (filtros.page > 0) params.set('page', String(filtros.page))
  const texto = params.toString()
  return texto ? `?${texto}` : ''
}

export function paramsDaApi(filtros: Filtros): string {
  const params = new URLSearchParams({
    status: filtros.status,
    page: String(filtros.page),
    size: String(TAMANHO_PAGINA),
    sort: filtros.sort,
  })
  if (filtros.q) params.set('q', filtros.q)
  if (filtros.uf) params.set('uf', filtros.uf)
  if (filtros.banca) params.set('banca', filtros.banca)
  if (filtros.salarioMin != null) params.set('salarioMin', String(filtros.salarioMin))
  return params.toString()
}

export function temFiltroAtivo(filtros: Filtros): boolean {
  return Boolean(
    filtros.q || filtros.uf || filtros.banca || filtros.salarioMin != null || filtros.status !== FILTROS_PADRAO.status,
  )
}

export function limparFiltros(filtros: Filtros): Filtros {
  return { ...FILTROS_PADRAO, sort: filtros.sort }
}

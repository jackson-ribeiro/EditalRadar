import type { StatusConcurso } from './api/tipos'

export const UFS = [
  'AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA',
  'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO',
] as const

export const NACIONAL = 'NACIONAL'

export type Ordenacao = 'fimInscricao,asc' | 'salarioMax,desc' | 'primeiraVezVistoEm,desc'

export const ORDENACOES: ReadonlyArray<{ valor: Ordenacao; rotulo: string }> = [
  { valor: 'fimInscricao,asc', rotulo: 'Prazo mais próximo' },
  { valor: 'salarioMax,desc', rotulo: 'Maior salário' },
  { valor: 'primeiraVezVistoEm,desc', rotulo: 'Vistos mais recentemente' },
]

export const PISOS_SALARIO: readonly number[] = [3000, 5000, 8000, 12000, 20000]

export const STATUS: ReadonlyArray<{ valor: StatusConcurso; rotulo: string; singular: string }> = [
  { valor: 'ABERTO', rotulo: 'Abertos', singular: 'aberto' },
  { valor: 'PREVISTO', rotulo: 'Previstos', singular: 'previsto' },
  { valor: 'ENCERRADO', rotulo: 'Encerrados', singular: 'encerrado' },
]

export const BANCAS_NAO_FILTRAVEIS: readonly string[] = ['Outras', 'Não informada']

export function rotuloUf(uf: string): string {
  return uf === NACIONAL ? 'Nacional' : uf
}

export function rotuloPiso(piso: number): string {
  return `a partir de R$ ${piso / 1000} mil`
}

export function singularStatus(status: StatusConcurso): string {
  return STATUS.find((item) => item.valor === status)?.singular ?? status.toLowerCase()
}

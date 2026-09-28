import type { Concurso } from './api/tipos'

export type NivelUrgencia = 'sem-data' | 'encerrado' | 'vencendo' | 'prazo' | 'tranquilo'

const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const PARTES_SAO_PAULO = new Intl.DateTimeFormat('en-CA', {
  timeZone: 'America/Sao_Paulo',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hourCycle: 'h23',
})
const UM_DIA_MS = 86_400_000

export function formatarMoeda(valor: number | null): string {
  return valor == null ? '—' : MOEDA.format(valor)
}

export function formatarData(iso: string | null): string {
  if (!iso) return '—'
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

function partes(data: Date) {
  const mapa = Object.fromEntries(PARTES_SAO_PAULO.formatToParts(data).map((parte) => [parte.type, parte.value]))
  return {
    chave: `${mapa.year}-${mapa.month}-${mapa.day}`,
    dia: mapa.day,
    mes: mapa.month,
    hora: `${mapa.hour}:${mapa.minute}`,
  }
}

export function descreverMomento(iso: string, agora: Date): string {
  const alvo = partes(new Date(iso))
  if (alvo.chave === partes(agora).chave) return `hoje às ${alvo.hora}`
  if (alvo.chave === partes(new Date(agora.getTime() - UM_DIA_MS)).chave) return `ontem às ${alvo.hora}`
  return `em ${alvo.dia}/${alvo.mes} às ${alvo.hora}`
}

export function minutosDesde(iso: string, agora: Date): number {
  return Math.max(0, Math.floor((agora.getTime() - new Date(iso).getTime()) / 60_000))
}

export function descreverDuracao(minutos: number): string {
  return minutos < 1 ? 'menos de 1 min' : `${minutos} min`
}

export function nivelUrgencia(dias: number | null): NivelUrgencia {
  if (dias == null) return 'sem-data'
  if (dias < 0) return 'encerrado'
  if (dias <= 7) return 'vencendo'
  if (dias <= 14) return 'prazo'
  return 'tranquilo'
}

export function textoDias(dias: number | null): string {
  if (dias == null) return 'sem data'
  if (dias < 0) return 'encerrado'
  if (dias === 0) return 'encerra hoje'
  return dias === 1 ? '1 dia' : `${dias} dias`
}

export function larguraRegua(dias: number): number {
  return Math.max(4, Math.round((Math.min(dias, 60) / 60) * 100))
}

export function fraseDestaque(encerrando: number): { destaque: string | null; resto: string } {
  if (encerrando <= 0) {
    return { destaque: null, resto: 'Nenhum concurso de TI encerra as inscrições nos próximos 7 dias.' }
  }
  if (encerrando === 1) {
    return { destaque: '1 concurso de TI', resto: ' encerra as inscrições nos próximos 7 dias.' }
  }
  return { destaque: `${encerrando} concursos de TI`, resto: ' encerram as inscrições nos próximos 7 dias.' }
}

export function plural(quantidade: number, singular: string, pluralTexto: string): string {
  return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`
}

export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim()
}

export function descreverCargo(
  concurso: Pick<Concurso, 'cargo' | 'cargos' | 'cargosTi'>,
): { principal: string; complemento: string | null } {
  const { cargo, cargos, cargosTi } = concurso
  const generico = !cargo || normalizar(cargo).startsWith('varios cargos')
  if (cargosTi.length > 0 && (generico || cargos.length > 1)) {
    return { principal: cargosTi[0], complemento: complementoCargos(cargosTi.length - 1, cargos.length - cargosTi.length) }
  }
  if (!generico) return { principal: cargo, complemento: null }
  if (cargos.length === 1) return { principal: cargos[0], complemento: null }
  if (cargos.length > 1) return { principal: `Vários cargos (${cargos.length})`, complemento: null }
  return { principal: cargo ?? 'Cargo não informado', complemento: null }
}

function complementoCargos(outrosTi: number, outros: number): string | null {
  if (outrosTi > 0 && outros > 0) return `+${outrosTi} de TI · +${outros} ${outros === 1 ? 'outro' : 'outros'}`
  if (outrosTi > 0) return `+${outrosTi} de TI`
  if (outros > 0) return outros === 1 ? '+1 outro cargo' : `+${outros} outros cargos`
  return null
}

export function listaCargos(concurso: Pick<Concurso, 'cargos' | 'cargosTi'>): string | undefined {
  if (concurso.cargos.length <= 1) return undefined
  const ti = new Set(concurso.cargosTi)
  return [...concurso.cargosTi, ...concurso.cargos.filter((cargo) => !ti.has(cargo))].join('\n')
}

export type ResumoValor =
  | { tipo: 'valor'; valor: number }
  | { tipo: 'maximo'; valor: number }
  | { tipo: 'trecho'; texto: string }
  | { tipo: 'ausente' }

type DadosResumo = Pick<
  Concurso,
  'cargo' | 'cargos' | 'cargosTi' | 'salarioMax' | 'trechoRemuneracao' | 'trechoTaxa' | 'taxaInscricao'
>

export function ehCargoUnico(concurso: Pick<Concurso, 'cargo' | 'cargos'>): boolean {
  const generico = concurso.cargo != null && normalizar(concurso.cargo).startsWith('varios cargos')
  return concurso.cargos.length <= 1 && !generico
}

export function resumoSalario(concurso: DadosResumo): ResumoValor {
  if (ehCargoUnico(concurso) && concurso.salarioMax != null) return { tipo: 'valor', valor: concurso.salarioMax }
  if (concurso.trechoRemuneracao) return { tipo: 'trecho', texto: concurso.trechoRemuneracao }
  if (concurso.salarioMax != null) return { tipo: 'maximo', valor: concurso.salarioMax }
  return { tipo: 'ausente' }
}

export function resumoTaxa(concurso: DadosResumo): ResumoValor {
  if (concurso.taxaInscricao != null) return { tipo: 'valor', valor: concurso.taxaInscricao }
  if (concurso.trechoTaxa) return { tipo: 'trecho', texto: concurso.trechoTaxa }
  return { tipo: 'ausente' }
}

export function descreverInscricoes(inicio: string | null, fim: string | null): string {
  if (inicio && fim) return `${formatarData(inicio)} a ${formatarData(fim)}`
  if (fim) return `até ${formatarData(fim)}`
  if (inicio) return `a partir de ${formatarData(inicio)}`
  return 'não informadas'
}

export function resumoCargos(concurso: DadosResumo): { titulo: string; itens: string[]; outros: number } {
  if (concurso.cargosTi.length > 0) {
    return {
      titulo: concurso.cargosTi.length === 1 ? 'Cargo de TI' : 'Cargos de TI',
      itens: concurso.cargosTi,
      outros: concurso.cargos.length - concurso.cargosTi.length,
    }
  }
  if (concurso.cargos.length > 0) return { titulo: 'Cargos', itens: concurso.cargos, outros: 0 }
  return { titulo: 'Cargo', itens: [concurso.cargo ?? 'Cargo não informado'], outros: 0 }
}

export function textoOutrosCargos(quantidade: number): string {
  return quantidade === 1 ? 'e mais 1 cargo de outra área' : `e mais ${quantidade} cargos de outras áreas`
}

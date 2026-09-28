import type { TotalPorFaixa } from '../api/tipos'
import type { EstadoRecurso } from '../hooks/useRecurso'
import { GraficoBarras, type ItemGrafico } from './GraficoBarras'

const PISOS: Record<string, { piso: number | null; eixo: string }> = {
  'Até R$ 3 mil': { piso: null, eixo: '≤3 mil' },
  'R$ 3–5 mil': { piso: 3000, eixo: '3–5' },
  'R$ 5–8 mil': { piso: 5000, eixo: '5–8' },
  'R$ 8–12 mil': { piso: 8000, eixo: '8–12' },
  'R$ 12–20 mil': { piso: 12000, eixo: '12–20' },
  'Acima de R$ 20 mil': { piso: 20000, eixo: '20+' },
}

export function GraficoFaixa({
  recurso,
  salarioMinAtivo,
  aoFiltrar,
}: {
  recurso: EstadoRecurso<TotalPorFaixa[]>
  salarioMinAtivo: number | null
  aoFiltrar: (salarioMin: number | null) => void
}) {
  const itens: ItemGrafico[] | null =
    recurso.dados?.map((linha) => {
      const faixa = PISOS[linha.faixa]
      return {
        chave: linha.faixa,
        rotulo: linha.faixa,
        rotuloEixo: faixa?.eixo ?? '?',
        total: linha.total,
        clicavel: faixa !== undefined,
        ativo: faixa?.piso != null && faixa.piso === salarioMinAtivo,
      }
    }) ?? null
  return (
    <GraficoBarras
      titulo="Por salário máximo"
      itens={itens}
      carregando={recurso.carregando}
      erro={recurso.erro}
      aoTentarDeNovo={recurso.recarregar}
      aoSelecionar={(chave) => {
        const piso = PISOS[chave]?.piso ?? null
        aoFiltrar(piso === salarioMinAtivo ? null : piso)
      }}
    />
  )
}

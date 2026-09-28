import type { TotalPorUf } from '../api/tipos'
import { NACIONAL, rotuloUf } from '../dominio'
import type { EstadoRecurso } from '../hooks/useRecurso'
import { GraficoBarras, type ItemGrafico } from './GraficoBarras'

const SEM_UF = 'NÃO INFORMADA'

export function GraficoUf({
  recurso,
  ufAtiva,
  aoFiltrar,
}: {
  recurso: EstadoRecurso<TotalPorUf[]>
  ufAtiva: string | null
  aoFiltrar: (uf: string | null) => void
}) {
  const itens: ItemGrafico[] | null =
    recurso.dados?.map((linha) => ({
      chave: linha.uf,
      rotulo: rotuloUf(linha.uf),
      rotuloEixo: linha.uf === NACIONAL ? 'Nac.' : linha.uf === SEM_UF ? '?' : linha.uf,
      total: linha.total,
      clicavel: linha.uf !== SEM_UF,
      ativo: linha.uf === ufAtiva,
    })) ?? null
  return (
    <GraficoBarras
      titulo="Por estado"
      itens={itens}
      carregando={recurso.carregando}
      erro={recurso.erro}
      aoTentarDeNovo={recurso.recarregar}
      aoSelecionar={(uf) => aoFiltrar(uf === ufAtiva ? null : uf)}
    />
  )
}

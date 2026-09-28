import { STATUS, rotuloPiso, rotuloUf } from '../dominio'
import { FILTROS_PADRAO, temFiltroAtivo, type Filtros, type MudancaFiltros } from '../filtros'
import { plural } from '../formatos'

interface Props {
  filtros: Filtros
  total: number | null
  aoAlterar: (mudancas: MudancaFiltros) => void
  aoLimpar: () => void
}

export function EtiquetasFiltro({ filtros, total, aoAlterar, aoLimpar }: Props) {
  const etiquetas: Array<{ texto: string; remover: MudancaFiltros }> = []
  if (filtros.q) etiquetas.push({ texto: `“${filtros.q}”`, remover: { q: '' } })
  if (filtros.uf) etiquetas.push({ texto: rotuloUf(filtros.uf), remover: { uf: null } })
  if (filtros.status !== FILTROS_PADRAO.status) {
    const rotulo = STATUS.find((item) => item.valor === filtros.status)?.rotulo ?? filtros.status
    etiquetas.push({ texto: rotulo, remover: { status: FILTROS_PADRAO.status } })
  }
  if (filtros.banca) etiquetas.push({ texto: filtros.banca, remover: { banca: null } })
  if (filtros.salarioMin != null) etiquetas.push({ texto: rotuloPiso(filtros.salarioMin), remover: { salarioMin: null } })

  return (
    <div className="flex min-h-8 flex-wrap items-center gap-2 pt-3 text-[13px]">
      {etiquetas.map((etiqueta) => (
        <button
          key={etiqueta.texto}
          type="button"
          aria-label={`Remover filtro ${etiqueta.texto}`}
          onClick={() => aoAlterar(etiqueta.remover)}
          className="rounded-full border border-caneta px-3 py-0.5 text-caneta hover:bg-caneta/10"
        >
          {etiqueta.texto} <span aria-hidden="true">✕</span>
        </button>
      ))}
      {temFiltroAtivo(filtros) && (
        <button type="button" onClick={aoLimpar} className="text-grafite underline underline-offset-4 hover:text-papel">
          Limpar filtros
        </button>
      )}
      {total != null && <span className="ml-auto text-grafite">{plural(total, 'concurso', 'concursos')}</span>}
    </div>
  )
}

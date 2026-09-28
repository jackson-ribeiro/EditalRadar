import { useCallback, useEffect, useState } from 'react'
import type { Concurso, Pagina, Resumo, TotalPorBanca, TotalPorFaixa, TotalPorUf } from './api/tipos'
import { Cabecalho } from './componentes/Cabecalho'
import { EtiquetasFiltro } from './componentes/EtiquetasFiltro'
import { FaixaResumo } from './componentes/FaixaResumo'
import { FiltrosTabela } from './componentes/FiltrosTabela'
import { FraseDestaque } from './componentes/FraseDestaque'
import { GraficoFaixa } from './componentes/GraficoFaixa'
import { GraficoUf } from './componentes/GraficoUf'
import { Panorama } from './componentes/Panorama'
import { TabelaConcursos } from './componentes/TabelaConcursos'
import { BANCAS_NAO_FILTRAVEIS } from './dominio'
import { paramsDaApi, temFiltroAtivo } from './filtros'
import { useFiltrosUrl } from './hooks/useFiltrosUrl'
import { useRecurso } from './hooks/useRecurso'
import { useSincronizacao } from './hooks/useSincronizacao'

function useAgora(intervaloMs = 30_000): Date {
  const [agora, setAgora] = useState(() => new Date())
  useEffect(() => {
    const intervalo = window.setInterval(() => setAgora(new Date()), intervaloMs)
    return () => window.clearInterval(intervalo)
  }, [intervaloMs])
  return agora
}

export default function App() {
  const { filtros, alterar, irParaPagina, limpar } = useFiltrosUrl()
  const [versao, setVersao] = useState(0)
  const agora = useAgora()
  const sync = useSincronizacao(useCallback(() => setVersao((valor) => valor + 1), []))

  const resumo = useRecurso<Resumo>('/api/dashboard/resumo', versao)
  const concursos = useRecurso<Pagina<Concurso>>(`/api/concursos?${paramsDaApi(filtros)}`, versao)
  const porUf = useRecurso<TotalPorUf[]>('/api/dashboard/por-uf', versao)
  const porBanca = useRecurso<TotalPorBanca[]>('/api/dashboard/por-banca', versao)
  const porFaixa = useRecurso<TotalPorFaixa[]>('/api/dashboard/por-faixa-salarial', versao)

  const paginaDados = concursos.dados
  useEffect(() => {
    if (!paginaDados || paginaDados.content.length > 0 || paginaDados.totalElements === 0) return
    if (filtros.page >= paginaDados.totalPages) {
      irParaPagina(Math.max(0, paginaDados.totalPages - 1), { substituir: true })
    }
  }, [paginaDados, filtros.page, irParaPagina])

  const bancas = (porBanca.dados ?? []).map((linha) => linha.banca).filter((banca) => !BANCAS_NAO_FILTRAVEIS.includes(banca))

  return (
    <main className="mx-auto max-w-[1200px] px-4 py-6 md:px-8 md:py-8">
      <Cabecalho sync={sync} agora={agora} />
      <FraseDestaque resumo={resumo.dados} />
      <FaixaResumo recurso={resumo} />
      <Panorama>
        <GraficoUf recurso={porUf} ufAtiva={filtros.uf} aoFiltrar={(uf) => alterar({ uf })} />
        <GraficoFaixa
          recurso={porFaixa}
          salarioMinAtivo={filtros.salarioMin}
          aoFiltrar={(salarioMin) => alterar({ salarioMin })}
        />
      </Panorama>

      <section aria-labelledby="titulo-concursos" className="mt-8">
        <h2 id="titulo-concursos" className="sr-only">
          Concursos de TI
        </h2>
        <FiltrosTabela filtros={filtros} bancas={bancas} aoAlterar={alterar} />
        <EtiquetasFiltro
          filtros={filtros}
          total={concursos.dados?.totalElements ?? null}
          aoAlterar={alterar}
          aoLimpar={limpar}
        />
        <TabelaConcursos
          key={paramsDaApi(filtros)}
          pagina={concursos.dados}
          carregando={concursos.carregando}
          erro={concursos.erro}
          aoTentarDeNovo={concursos.recarregar}
          status={filtros.status}
          filtroAtivo={temFiltroAtivo(filtros)}
          aoLimparFiltros={limpar}
          aoMudarPagina={irParaPagina}
        />
      </section>

    </main>
  )
}

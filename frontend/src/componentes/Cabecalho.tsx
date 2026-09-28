import type { Coleta } from '../api/tipos'
import { descreverDuracao, descreverMomento, minutosDesde } from '../formatos'
import type { EstadoSincronizacao } from '../hooks/useSincronizacao'

function situacao(ultima: Coleta | null, coletando: boolean, carregando: boolean, agora: Date) {
  if (coletando) {
    return {
      texto: ultima ? `Coletando… iniciada há ${descreverDuracao(minutosDesde(ultima.iniciadaEm, agora))}` : 'Coletando…',
      falhou: false,
    }
  }
  if (!ultima) return { texto: carregando ? '' : 'Ainda não houve coleta', falhou: false }
  if (ultima.status === 'FALHA') {
    return { texto: `A última coleta falhou: ${ultima.mensagemErro ?? 'motivo não informado'}`, falhou: true }
  }
  const momento = ultima.finalizadaEm ?? ultima.iniciadaEm
  return {
    texto: minutosDesde(momento, agora) < 1 ? 'Coletado agora' : `Coletado ${descreverMomento(momento, agora)}`,
    falhou: false,
  }
}

export function Cabecalho({ sync, agora }: { sync: EstadoSincronizacao; agora: Date }) {
  const { texto, falhou } = situacao(sync.ultima, sync.coletando, sync.carregandoUltima, agora)
  const rotuloBotao = sync.coletando ? 'Coletando…' : falhou ? 'Tentar de novo' : 'Atualizar agora'
  return (
    <header className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <h1 className="font-titulo text-lg font-bold tracking-wide">EditalRadar</h1>
      <div className="flex flex-col gap-1 md:items-end">
        <div className="flex flex-wrap items-center gap-3 text-[13px]">
          <span className={falhou ? 'text-vencendo' : 'text-grafite'}>{texto}</span>
          <button
            type="button"
            onClick={sync.atualizar}
            disabled={sync.coletando}
            className="rounded-md border border-caneta px-3 py-1 text-caneta hover:bg-caneta/10 disabled:cursor-wait disabled:border-linha disabled:text-grafite disabled:hover:bg-transparent"
          >
            {rotuloBotao}
          </button>
        </div>
        {sync.erro && (
          <p role="alert" className="text-[13px] text-vencendo">
            {sync.erro}
          </p>
        )}
      </div>
    </header>
  )
}

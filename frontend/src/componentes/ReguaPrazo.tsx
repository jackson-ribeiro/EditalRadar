import { formatarData, larguraRegua, nivelUrgencia, textoDias, type NivelUrgencia } from '../formatos'

const COR_BARRA: Partial<Record<NivelUrgencia, string>> = {
  vencendo: 'bg-vencendo',
  prazo: 'bg-prazo',
  tranquilo: 'bg-grafite',
}

const COR_TEXTO: Record<NivelUrgencia, string> = {
  vencendo: 'text-vencendo',
  prazo: 'text-prazo',
  tranquilo: 'text-papel',
  'sem-data': 'text-grafite',
  encerrado: 'text-grafite',
}

function descrever(dias: number | null, fim: string | null): string {
  const data = fim ? `, em ${formatarData(fim)}` : ''
  if (dias == null) return 'Sem data de encerramento das inscrições'
  if (dias < 0) return `Inscrições encerradas${fim ? ` em ${formatarData(fim)}` : ''}`
  if (dias === 0) return `Inscrições encerram hoje${data}`
  return `Inscrições encerram em ${textoDias(dias)}${data}`
}

export function ReguaPrazo({ dias, fim }: { dias: number | null; fim: string | null }) {
  const nivel = nivelUrgencia(dias)
  const corBarra = COR_BARRA[nivel]
  return (
    <div role="img" aria-label={descrever(dias, fim)} data-nivel={nivel} className="flex items-center justify-end gap-3">
      {corBarra && dias != null && (
        <div className="h-1.5 min-w-24 flex-1 overflow-hidden rounded-full bg-linha" aria-hidden="true">
          <span
            data-parte="barra"
            className={`block h-full rounded-full ${corBarra}`}
            style={{ width: `${larguraRegua(dias)}%` }}
          />
        </div>
      )}
      <span aria-hidden="true" className={`w-24 shrink-0 text-right text-[13px] ${COR_TEXTO[nivel]}`}>
        {textoDias(dias)}
      </span>
    </div>
  )
}

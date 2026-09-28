import type { MouseEvent } from 'react'
import type { Concurso } from '../api/tipos'
import { descreverCargo, formatarMoeda } from '../formatos'
import { PainelResumo } from './PainelResumo'
import { ReguaPrazo } from './ReguaPrazo'

const CELULA = 'block px-0 py-0.5 md:table-cell md:px-3 md:py-3 md:align-middle'
const CELULA_INLINE =
  'inline text-[13px] text-grafite md:table-cell md:px-3 md:py-3 md:align-middle md:text-sm md:text-papel'

interface Props {
  concurso: Concurso
  aberto: boolean
  aoAlternar: () => void
}

export function LinhaConcurso({ concurso, aberto, aoAlternar }: Props) {
  const cargo = descreverCargo(concurso.cargo, concurso.cargos)
  const titulo = concurso.cargos.length > 1 ? concurso.cargos.join('\n') : undefined
  const semVagasComReserva = concurso.vagas == null && concurso.cadastroReserva
  const idPainel = `resumo-${concurso.id}`

  const aoClicarNaLinha = (evento: MouseEvent<HTMLTableRowElement>) => {
    if ((evento.target as HTMLElement).closest('a, button')) return
    if (window.getSelection()?.toString()) return
    aoAlternar()
  }

  return (
    <>
      <tr
        onClick={aoClicarNaLinha}
        className={`relative block cursor-pointer py-3 hover:bg-mesa/60 md:table-row md:py-0 ${aberto ? '' : 'border-b border-linha'}`}
      >
        <td className="absolute top-3 right-0 md:static md:table-cell md:w-8 md:px-2 md:py-3 md:align-middle">
          <button
            type="button"
            aria-expanded={aberto}
            aria-controls={aberto ? idPainel : undefined}
            aria-label={`${aberto ? 'Ocultar' : 'Ver'} resumo de ${concurso.orgao}`}
            onClick={aoAlternar}
            className="rounded px-1.5 py-0.5 text-caneta hover:bg-caneta/10"
          >
            <span aria-hidden="true">{aberto ? '▾' : '▸'}</span>
          </button>
        </td>
        <td className={`${CELULA} pr-8 md:w-[40%] md:pr-3`}>
          <a
            href={concurso.urlOrigem}
            target="_blank"
            rel="noreferrer"
            className="text-papel underline-offset-4 hover:text-caneta hover:underline"
          >
            {concurso.orgao}
          </a>
          {concurso.novo && <span className="ml-2 text-xs font-bold text-caneta">novo</span>}
          <span className="mt-0.5 block text-[13px] text-grafite" title={titulo}>
            {cargo}
          </span>
        </td>
        <td className={`${CELULA_INLINE} pr-1`}>{concurso.nacional ? 'Nacional' : (concurso.uf ?? '—')}</td>
        <td className={CELULA_INLINE}>
          <span aria-hidden="true" className="md:hidden">
            ·{' '}
          </span>
          {concurso.banca ?? '—'}
        </td>
        <td className={CELULA}>
          {formatarMoeda(concurso.salarioMax)}
          {semVagasComReserva && <span className="block text-[12.5px] text-grafite">cadastro reserva</span>}
        </td>
        <td className={`${CELULA} pt-2 md:w-[22%] md:pt-3`}>
          <ReguaPrazo dias={concurso.diasRestantes} fim={concurso.fimInscricao} />
        </td>
      </tr>
      {aberto && (
        <tr className="block border-b border-linha md:table-row">
          <td colSpan={6} className="block md:table-cell md:px-3">
            <PainelResumo concurso={concurso} id={idPainel} />
          </td>
        </tr>
      )}
    </>
  )
}

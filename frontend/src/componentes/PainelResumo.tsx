import type { Concurso } from '../api/tipos'
import {
  descreverInscricoes,
  formatarData,
  formatarMoeda,
  resumoCargos,
  resumoSalario,
  resumoTaxa,
  textoOutrosCargos,
  type ResumoValor,
} from '../formatos'

const ROTULO = 'mt-2 text-[13px] text-grafite first:mt-0 sm:mt-0'

function Valor({ resumo, ausente }: { resumo: ResumoValor; ausente: string }) {
  switch (resumo.tipo) {
    case 'valor':
      return <span className="text-[17px] font-bold">{formatarMoeda(resumo.valor)}</span>
    case 'maximo':
      return (
        <span>
          até {formatarMoeda(resumo.valor)} <span className="text-grafite">(valor máximo da listagem)</span>
        </span>
      )
    case 'trecho':
      return (
        <blockquote className="border-l border-linha pl-3 text-[13.5px] leading-relaxed">
          {resumo.texto}
          <span className="mt-0.5 block text-xs text-grafite">trecho da notícia</span>
        </blockquote>
      )
    case 'ausente':
      return <span className="text-grafite">{ausente}</span>
  }
}

export function PainelResumo({ concurso, id }: { concurso: Concurso; id: string }) {
  const cargos = resumoCargos(concurso)
  return (
    <div id={id} className="my-3 grid gap-6 border-l-2 border-caneta py-1 pl-4 md:ml-8 md:grid-cols-[1fr_1.25fr]">
      <div>
        <h3 className="mb-2 font-titulo text-[13px] font-bold">{cargos.titulo}</h3>
        <ul className="space-y-1 text-sm">
          {cargos.itens.map((item, indice) => (
            <li key={`${indice}-${item}`}>{item}</li>
          ))}
        </ul>
        {cargos.outros > 0 && <p className="mt-2 text-[12.5px] text-grafite">{textoOutrosCargos(cargos.outros)}</p>}
      </div>
      <dl className="grid grid-cols-1 gap-x-4 gap-y-1 text-sm sm:grid-cols-[8.5rem_1fr] sm:gap-y-2">
        <dt className={ROTULO}>Salário</dt>
        <dd>
          <Valor resumo={resumoSalario(concurso)} ausente="não informado na notícia" />
        </dd>
        <dt className={ROTULO}>Taxa de inscrição</dt>
        <dd>
          <Valor resumo={resumoTaxa(concurso)} ausente="não informada na notícia" />
        </dd>
        <dt className={ROTULO}>Escolaridade</dt>
        <dd>{concurso.escolaridade ?? 'não informada'}</dd>
        <dt className={ROTULO}>Inscrições</dt>
        <dd>{descreverInscricoes(concurso.inicioInscricao, concurso.fimInscricao)}</dd>
        <dt className={ROTULO}>Prova</dt>
        <dd>{concurso.dataProva ? formatarData(concurso.dataProva) : 'não informada na notícia'}</dd>
        <dt className={ROTULO}>Banca</dt>
        <dd>
          {concurso.banca ?? 'não identificada'}
          <span className="text-grafite"> · </span>
          <a
            href={concurso.urlOrigem}
            target="_blank"
            rel="noreferrer"
            className="text-caneta underline-offset-4 hover:underline"
          >
            Abrir notícia no PCI
          </a>
        </dd>
      </dl>
    </div>
  )
}

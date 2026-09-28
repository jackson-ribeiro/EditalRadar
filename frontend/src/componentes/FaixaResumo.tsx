import type { Resumo } from '../api/tipos'
import type { EstadoRecurso } from '../hooks/useRecurso'
import { Carregando, Erro } from './Estados'

function Numero({ valor, singular, pluralTexto }: { valor: number; singular: string; pluralTexto: string }) {
  return (
    <div className="border-linha py-3 md:flex-1 md:border-l md:pl-4 md:first:border-l-0 md:first:pl-0">
      <strong className="block text-[22px] font-bold text-papel">{valor}</strong>
      <span className="text-[13px] text-grafite">{valor === 1 ? singular : pluralTexto}</span>
    </div>
  )
}

export function FaixaResumo({ recurso }: { recurso: EstadoRecurso<Resumo> }) {
  if (recurso.erro) return <Erro mensagem={recurso.erro.message} aoTentarDeNovo={recurso.recarregar} />
  const resumo = recurso.dados
  if (!resumo) return <Carregando rotulo="Carregando resumo" linhas={1} />
  return (
    <div className="grid grid-cols-2 gap-x-4 border-y border-linha md:flex">
      <Numero valor={resumo.abertos} singular="aberto" pluralTexto="abertos" />
      <Numero valor={resumo.previstos} singular="previsto" pluralTexto="previstos" />
      <Numero valor={resumo.encerrandoEm7Dias} singular="encerra em 7 dias" pluralTexto="encerram em 7 dias" />
      <Numero valor={resumo.novosNaSemana} singular="novo na semana" pluralTexto="novos na semana" />
    </div>
  )
}

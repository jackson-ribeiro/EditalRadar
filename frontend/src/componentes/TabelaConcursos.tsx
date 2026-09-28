import { useState } from 'react'
import type { ErroApi } from '../api/cliente'
import type { Concurso, Pagina, StatusConcurso } from '../api/tipos'
import { singularStatus } from '../dominio'
import { Carregando, Erro, Vazio } from './Estados'
import { LinhaConcurso } from './LinhaConcurso'
import { Paginacao } from './Paginacao'

interface Props {
  pagina: Pagina<Concurso> | null
  carregando: boolean
  erro: ErroApi | null
  aoTentarDeNovo: () => void
  status: StatusConcurso
  filtroAtivo: boolean
  aoLimparFiltros: () => void
  aoMudarPagina: (page: number) => void
}

const CABECALHO = 'px-3 pb-2 text-left text-[13px] font-medium text-grafite'

export function TabelaConcursos(props: Props) {
  const [abertos, setAbertos] = useState<ReadonlySet<number>>(() => new Set())
  const alternar = (id: number) =>
    setAbertos((atuais) => {
      const proximos = new Set(atuais)
      if (proximos.has(id)) {
        proximos.delete(id)
      } else {
        proximos.add(id)
      }
      return proximos
    })
  const { pagina, carregando, erro, aoTentarDeNovo, status, filtroAtivo, aoLimparFiltros, aoMudarPagina } = props

  if (erro) return <Erro mensagem={erro.message} aoTentarDeNovo={aoTentarDeNovo} />
  if (!pagina) return carregando ? <Carregando rotulo="Carregando concursos" /> : null

  if (pagina.content.length === 0) {
    const tipo = singularStatus(status)
    return filtroAtivo ? (
      <Vazio
        mensagem={`Nenhum concurso de TI ${tipo} com esses filtros.`}
        acao={{ rotulo: 'Limpar filtros', aoClicar: aoLimparFiltros }}
      />
    ) : (
      <Vazio mensagem={`Nenhum concurso de TI ${tipo} por enquanto. Clique em Atualizar agora para coletar.`} />
    )
  }

  return (
    <div aria-busy={carregando}>
      <table className="block w-full border-collapse md:table">
        <thead className="hidden md:table-header-group">
          <tr className="border-b border-linha">
            <th scope="col" className="w-8">
              <span className="sr-only">Resumo</span>
            </th>
            <th scope="col" className={CABECALHO}>Concurso</th>
            <th scope="col" className={CABECALHO}>UF</th>
            <th scope="col" className={CABECALHO}>Banca</th>
            <th scope="col" className={CABECALHO}>Salário até</th>
            <th scope="col" className={CABECALHO}>Inscrições</th>
          </tr>
        </thead>
        <tbody className="block md:table-row-group">
          {pagina.content.map((concurso) => (
            <LinhaConcurso
              key={concurso.id}
              concurso={concurso}
              aberto={abertos.has(concurso.id)}
              aoAlternar={() => alternar(concurso.id)}
            />
          ))}
        </tbody>
      </table>
      <Paginacao page={pagina.page} totalPages={pagina.totalPages} aoMudar={aoMudarPagina} />
    </div>
  )
}

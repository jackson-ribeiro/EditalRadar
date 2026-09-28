import { useEffect, useState, type ReactNode } from 'react'
import type { StatusConcurso } from '../api/tipos'
import { NACIONAL, ORDENACOES, PISOS_SALARIO, STATUS, UFS, rotuloPiso, type Ordenacao } from '../dominio'
import type { Filtros, MudancaFiltros } from '../filtros'

export const ESPERA_BUSCA_MS = 350

interface Props {
  filtros: Filtros
  bancas: string[]
  aoAlterar: (mudancas: MudancaFiltros, opcoes?: { substituir?: boolean }) => void
}

const CAMPO =
  'rounded-md border border-linha bg-mesa px-3 py-2 text-sm text-papel focus-visible:border-caneta'

function Seletor({
  rotulo,
  valor,
  aoMudar,
  children,
}: {
  rotulo: string
  valor: string
  aoMudar: (valor: string) => void
  children: ReactNode
}) {
  return (
    <label className="flex flex-col">
      <span className="sr-only">{rotulo}</span>
      <select className={CAMPO} value={valor} onChange={(evento) => aoMudar(evento.target.value)}>
        {children}
      </select>
    </label>
  )
}

export function FiltrosTabela({ filtros, bancas, aoAlterar }: Props) {
  const [texto, setTexto] = useState(filtros.q)
  const [abertoNoCelular, setAbertoNoCelular] = useState(false)

  useEffect(() => {
    setTexto(filtros.q)
  }, [filtros.q])

  useEffect(() => {
    if (texto.trim() === filtros.q) return
    const espera = window.setTimeout(() => aoAlterar({ q: texto.trim() }, { substituir: true }), ESPERA_BUSCA_MS)
    return () => window.clearTimeout(espera)
  }, [texto, filtros.q, aoAlterar])

  const opcoesBanca = filtros.banca && !bancas.includes(filtros.banca) ? [...bancas, filtros.banca] : bancas

  return (
    <div className="flex flex-col gap-2 md:flex-row md:flex-wrap md:items-center">
      <div className="flex gap-2 md:flex-1">
        <label className="flex flex-1 flex-col">
          <span className="sr-only">Buscar órgão ou cargo</span>
          <input
            type="search"
            className={`${CAMPO} w-full placeholder:text-grafite`}
            placeholder="Buscar órgão ou cargo…"
            value={texto}
            onChange={(evento) => setTexto(evento.target.value)}
          />
        </label>
        <button
          type="button"
          className="rounded-md border border-linha px-3 text-sm text-papel md:hidden"
          aria-expanded={abertoNoCelular}
          onClick={() => setAbertoNoCelular((valor) => !valor)}
        >
          Filtros
        </button>
      </div>
      <div className={`${abertoNoCelular ? 'grid' : 'hidden'} grid-cols-2 gap-2 md:flex md:flex-wrap`}>
        <Seletor rotulo="UF" valor={filtros.uf ?? ''} aoMudar={(valor) => aoAlterar({ uf: valor || null })}>
          <option value="">Todas as UFs</option>
          <option value={NACIONAL}>Nacional</option>
          {UFS.map((uf) => (
            <option key={uf} value={uf}>
              {uf}
            </option>
          ))}
        </Seletor>
        <Seletor rotulo="Status" valor={filtros.status} aoMudar={(valor) => aoAlterar({ status: valor as StatusConcurso })}>
          {STATUS.map((item) => (
            <option key={item.valor} value={item.valor}>
              {item.rotulo}
            </option>
          ))}
        </Seletor>
        <Seletor rotulo="Banca" valor={filtros.banca ?? ''} aoMudar={(valor) => aoAlterar({ banca: valor || null })}>
          <option value="">Todas as bancas</option>
          {opcoesBanca.map((banca) => (
            <option key={banca} value={banca}>
              {banca}
            </option>
          ))}
        </Seletor>
        <Seletor
          rotulo="Salário"
          valor={filtros.salarioMin == null ? '' : String(filtros.salarioMin)}
          aoMudar={(valor) => aoAlterar({ salarioMin: valor ? Number(valor) : null })}
        >
          <option value="">Qualquer salário</option>
          {PISOS_SALARIO.map((piso) => (
            <option key={piso} value={piso}>
              {rotuloPiso(piso)}
            </option>
          ))}
        </Seletor>
        <Seletor rotulo="Ordenar" valor={filtros.sort} aoMudar={(valor) => aoAlterar({ sort: valor as Ordenacao })}>
          {ORDENACOES.map((item) => (
            <option key={item.valor} value={item.valor}>
              {item.rotulo}
            </option>
          ))}
        </Seletor>
      </div>
    </div>
  )
}

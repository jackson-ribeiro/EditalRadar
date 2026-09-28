import type { ReactNode } from 'react'
import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { ErroApi } from '../api/cliente'
import { Carregando, Erro, Vazio } from './Estados'

export interface ItemGrafico {
  chave: string
  rotulo: string
  rotuloEixo: string
  total: number
  clicavel: boolean
  ativo: boolean
}

interface Props {
  titulo: string
  itens: ItemGrafico[] | null
  carregando: boolean
  erro: ErroApi | null
  aoTentarDeNovo: () => void
  aoSelecionar: (chave: string) => void
}

function cor(item: ItemGrafico): string {
  return item.ativo ? 'var(--color-caneta)' : 'var(--color-grafite)'
}

function opacidade(item: ItemGrafico): number {
  if (item.ativo) return 1
  return item.clicavel ? 0.55 : 0.25
}

const EIXO = { fill: 'var(--color-grafite)', fontSize: 11 }

export function GraficoBarras({ titulo, itens, carregando, erro, aoTentarDeNovo, aoSelecionar }: Props) {
  const clicar = (item: ItemGrafico | undefined) => {
    if (item?.clicavel) aoSelecionar(item.chave)
  }

  let conteudo: ReactNode
  if (erro) {
    conteudo = <Erro mensagem={erro.message} aoTentarDeNovo={aoTentarDeNovo} />
  } else if (!itens) {
    conteudo = carregando ? <Carregando rotulo={`Carregando gráfico ${titulo.toLowerCase()}`} linhas={3} /> : null
  } else if (itens.every((item) => item.total === 0)) {
    conteudo = <Vazio mensagem="Sem concursos abertos para agrupar." />
  } else {
    conteudo = (
      <>
        <div aria-hidden="true" style={{ height: 110 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={itens} margin={{ left: 0, right: 12 }}>
              <XAxis dataKey="rotuloEixo" tick={EIXO} interval={0} axisLine={{ stroke: 'var(--color-linha)' }} tickLine={false} />
              <YAxis hide allowDecimals={false} />
              <Tooltip
                cursor={{ fill: 'var(--color-linha)', opacity: 0.4 }}
                contentStyle={{ background: 'var(--color-mesa)', border: '1px solid var(--color-linha)', borderRadius: 6 }}
                labelStyle={{ color: 'var(--color-papel)' }}
                itemStyle={{ color: 'var(--color-papel)' }}
                formatter={(valor) => [valor, 'concursos']}
              />
              <Bar dataKey="total" isAnimationActive={false} radius={[3, 3, 0, 0]} onClick={(_dado, indice) => clicar(itens[indice])}>
                {itens.map((item) => (
                  <Cell
                    key={item.chave}
                    fill={cor(item)}
                    fillOpacity={opacidade(item)}
                    cursor={item.clicavel ? 'pointer' : 'default'}
                  />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
        <ul className="sr-only focus-within:not-sr-only focus-within:mt-3 focus-within:flex focus-within:flex-wrap focus-within:gap-2">
          {itens.map((item) => (
            <li key={item.chave}>
              {item.clicavel ? (
                <button
                  type="button"
                  aria-pressed={item.ativo}
                  onClick={() => clicar(item)}
                  className="rounded-md border border-linha px-2 py-1 text-[13px] text-papel aria-pressed:border-caneta aria-pressed:text-caneta"
                >
                  {`${item.rotulo}: ${item.total}`}
                </button>
              ) : (
                <span>{`${item.rotulo}: ${item.total}`}</span>
              )}
            </li>
          ))}
        </ul>
      </>
    )
  }

  return (
    <section className="min-w-0">
      <h3 className="mb-2 text-[13px] text-grafite">{titulo}</h3>
      {conteudo}
    </section>
  )
}

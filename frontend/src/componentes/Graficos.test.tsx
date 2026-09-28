import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { TotalPorFaixa, TotalPorUf } from '../api/tipos'
import type { EstadoRecurso } from '../hooks/useRecurso'
import { GraficoFaixa } from './GraficoFaixa'
import { GraficoUf } from './GraficoUf'

function recurso<T>(dados: T | null): EstadoRecurso<T> {
  return { dados, carregando: false, erro: null, recarregar: vi.fn() }
}

const FAIXAS: TotalPorFaixa[] = [
  { faixa: 'Até R$ 3 mil', total: 1 },
  { faixa: 'R$ 3–5 mil', total: 2 },
  { faixa: 'R$ 5–8 mil', total: 3 },
  { faixa: 'R$ 8–12 mil', total: 4 },
  { faixa: 'R$ 12–20 mil', total: 0 },
  { faixa: 'Acima de R$ 20 mil', total: 1 },
  { faixa: 'Não informado', total: 2 },
]

describe('GraficoUf', () => {
  const dados: TotalPorUf[] = [
    { uf: 'SP', total: 5 },
    { uf: 'NACIONAL', total: 2 },
    { uf: 'NÃO INFORMADA', total: 1 },
  ]

  it('clicar numa UF filtra; clicar na ativa remove', async () => {
    const aoFiltrar = vi.fn()
    const { rerender } = render(<GraficoUf recurso={recurso(dados)} ufAtiva={null} aoFiltrar={aoFiltrar} />)
    expect(screen.getByRole('heading', { name: 'Por estado' })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Nacional: 2' }))
    expect(aoFiltrar).toHaveBeenLastCalledWith('NACIONAL')

    rerender(<GraficoUf recurso={recurso(dados)} ufAtiva="SP" aoFiltrar={aoFiltrar} />)
    expect(screen.getByRole('button', { name: 'SP: 5' })).toHaveAttribute('aria-pressed', 'true')
    await userEvent.click(screen.getByRole('button', { name: 'SP: 5' }))
    expect(aoFiltrar).toHaveBeenLastCalledWith(null)
  })

  it('a lista acessível aparece quando recebe foco do teclado', () => {
    render(<GraficoUf recurso={recurso(dados)} ufAtiva={null} aoFiltrar={vi.fn()} />)
    const lista = screen.getByRole('button', { name: 'SP: 5' }).closest('ul')
    expect(lista).toHaveClass('sr-only', 'focus-within:not-sr-only')
  })

  it('"NÃO INFORMADA" não é clicável', () => {
    render(<GraficoUf recurso={recurso(dados)} ufAtiva={null} aoFiltrar={vi.fn()} />)
    expect(screen.queryByRole('button', { name: /NÃO INFORMADA/ })).not.toBeInTheDocument()
    expect(screen.getByText('NÃO INFORMADA: 1')).toBeInTheDocument()
  })

  it('sem dados mostra o vazio', () => {
    render(<GraficoUf recurso={recurso<TotalPorUf[]>([])} ufAtiva={null} aoFiltrar={vi.fn()} />)
    expect(screen.getByText('Sem concursos abertos para agrupar.')).toBeInTheDocument()
  })
})

describe('GraficoFaixa', () => {
  it('faixa aplica o piso como salário mínimo', async () => {
    const aoFiltrar = vi.fn()
    render(<GraficoFaixa recurso={recurso(FAIXAS)} salarioMinAtivo={null} aoFiltrar={aoFiltrar} />)

    await userEvent.click(screen.getByRole('button', { name: 'R$ 8–12 mil: 4' }))
    expect(aoFiltrar).toHaveBeenLastCalledWith(8000)
    await userEvent.click(screen.getByRole('button', { name: 'Até R$ 3 mil: 1' }))
    expect(aoFiltrar).toHaveBeenLastCalledWith(null)
    expect(screen.queryByRole('button', { name: /Não informado/ })).not.toBeInTheDocument()
  })

  it('a faixa do filtro ativo aparece pressionada e desfaz ao clicar', async () => {
    const aoFiltrar = vi.fn()
    render(<GraficoFaixa recurso={recurso(FAIXAS)} salarioMinAtivo={8000} aoFiltrar={aoFiltrar} />)
    const ativa = screen.getByRole('button', { name: 'R$ 8–12 mil: 4' })
    expect(ativa).toHaveAttribute('aria-pressed', 'true')
    await userEvent.click(ativa)
    expect(aoFiltrar).toHaveBeenLastCalledWith(null)
  })
})

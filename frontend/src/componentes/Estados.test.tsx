import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Carregando, Erro, Vazio } from './Estados'

describe('Estados', () => {
  it('Carregando é anunciado como status', () => {
    render(<Carregando rotulo="Carregando concursos" />)
    expect(screen.getByRole('status', { name: 'Carregando concursos' })).toBeInTheDocument()
  })

  it('Vazio mostra a mensagem e a ação opcional', async () => {
    const aoClicar = vi.fn()
    render(<Vazio mensagem="Nada por aqui." acao={{ rotulo: 'Limpar filtros', aoClicar }} />)
    expect(screen.getByText('Nada por aqui.')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Limpar filtros' }))
    expect(aoClicar).toHaveBeenCalledTimes(1)
  })

  it('Erro é um alerta com "Tentar de novo"', async () => {
    const aoTentarDeNovo = vi.fn()
    render(<Erro mensagem="O servidor não respondeu." aoTentarDeNovo={aoTentarDeNovo} />)
    expect(screen.getByRole('alert')).toHaveTextContent('O servidor não respondeu.')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))
    expect(aoTentarDeNovo).toHaveBeenCalledTimes(1)
  })
})

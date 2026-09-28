import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ReguaPrazo } from './ReguaPrazo'

function barra(container: HTMLElement) {
  return container.querySelector<HTMLElement>('[data-parte="barra"]')
}

describe('ReguaPrazo', () => {
  it('prazo vencendo: régua curta em vermelho', () => {
    const { container } = render(<ReguaPrazo dias={2} fim="2026-09-25" />)
    const regua = screen.getByRole('img', { name: 'Inscrições encerram em 2 dias, em 25/09/2026' })
    expect(regua).toHaveAttribute('data-nivel', 'vencendo')
    expect(barra(container)).toHaveStyle({ width: '4%' })
    expect(barra(container)).toHaveClass('bg-vencendo')
    expect(screen.getByText('2 dias')).toHaveClass('text-vencendo')
  })

  it('prazo de 8 a 14 dias em laranja', () => {
    const { container } = render(<ReguaPrazo dias={10} fim="2026-10-03" />)
    expect(screen.getByRole('img')).toHaveAttribute('data-nivel', 'prazo')
    expect(barra(container)).toHaveStyle({ width: '17%' })
    expect(barra(container)).toHaveClass('bg-prazo')
  })

  it('prazo tranquilo em grafite com régua cheia a partir de 60 dias', () => {
    const { container } = render(<ReguaPrazo dias={69} fim="2026-12-01" />)
    expect(barra(container)).toHaveStyle({ width: '100%' })
    expect(barra(container)).toHaveClass('bg-grafite')
  })

  it('encerra hoje', () => {
    render(<ReguaPrazo dias={0} fim="2026-09-23" />)
    expect(screen.getByRole('img', { name: 'Inscrições encerram hoje, em 23/09/2026' })).toBeInTheDocument()
    expect(screen.getByText('encerra hoje')).toBeInTheDocument()
  })

  it('sem data e encerrado não mostram régua', () => {
    const { container, rerender } = render(<ReguaPrazo dias={null} fim={null} />)
    expect(screen.getByRole('img', { name: 'Sem data de encerramento das inscrições' })).toBeInTheDocument()
    expect(screen.getByText('sem data')).toBeInTheDocument()
    expect(barra(container)).toBeNull()

    rerender(<ReguaPrazo dias={-3} fim="2026-09-20" />)
    expect(screen.getByRole('img', { name: 'Inscrições encerradas em 20/09/2026' })).toBeInTheDocument()
    expect(barra(container)).toBeNull()
  })
})

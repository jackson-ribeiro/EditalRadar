import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { Panorama } from './Panorama'

describe('Panorama', () => {
  it('no celular começa recolhido e abre pelo botão', async () => {
    render(<Panorama><p>gráficos</p></Panorama>)
    const botao = screen.getByRole('button', { name: 'Ver distribuição' })
    expect(botao).toHaveAttribute('aria-expanded', 'false')
    expect(screen.getByText('gráficos').parentElement).toHaveClass('hidden', 'md:grid')

    await userEvent.click(botao)
    expect(screen.getByRole('button', { name: 'Esconder distribuição' })).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByText('gráficos').parentElement).not.toHaveClass('hidden')
  })
})

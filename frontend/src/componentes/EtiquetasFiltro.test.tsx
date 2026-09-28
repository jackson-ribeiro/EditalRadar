import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { FILTROS_PADRAO } from '../filtros'
import { EtiquetasFiltro } from './EtiquetasFiltro'

describe('EtiquetasFiltro', () => {
  it('mostra uma etiqueta por filtro ativo e remove individualmente', async () => {
    const aoAlterar = vi.fn()
    render(
      <EtiquetasFiltro
        filtros={{ ...FILTROS_PADRAO, q: 'analista', uf: 'NACIONAL', status: 'PREVISTO', banca: 'Quadrix', salarioMin: 8000 }}
        total={12} aoAlterar={aoAlterar} aoLimpar={vi.fn()}
      />,
    )

    expect(screen.getByText('12 concursos')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Remover filtro Nacional' }))
    expect(aoAlterar).toHaveBeenLastCalledWith({ uf: null })
    await userEvent.click(screen.getByRole('button', { name: 'Remover filtro “analista”' }))
    expect(aoAlterar).toHaveBeenLastCalledWith({ q: '' })
    await userEvent.click(screen.getByRole('button', { name: 'Remover filtro Previstos' }))
    expect(aoAlterar).toHaveBeenLastCalledWith({ status: 'ABERTO' })
    await userEvent.click(screen.getByRole('button', { name: 'Remover filtro Quadrix' }))
    expect(aoAlterar).toHaveBeenLastCalledWith({ banca: null })
    await userEvent.click(screen.getByRole('button', { name: 'Remover filtro a partir de R$ 8 mil' }))
    expect(aoAlterar).toHaveBeenLastCalledWith({ salarioMin: null })
  })

  it('limpar filtros só aparece com filtro ativo', async () => {
    const aoLimpar = vi.fn()
    const { rerender } = render(
      <EtiquetasFiltro filtros={FILTROS_PADRAO} total={1} aoAlterar={vi.fn()} aoLimpar={aoLimpar} />,
    )
    expect(screen.getByText('1 concurso')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Limpar filtros' })).not.toBeInTheDocument()

    rerender(<EtiquetasFiltro filtros={{ ...FILTROS_PADRAO, uf: 'SP' }} total={null} aoAlterar={vi.fn()} aoLimpar={aoLimpar} />)
    await userEvent.click(screen.getByRole('button', { name: 'Limpar filtros' }))
    expect(aoLimpar).toHaveBeenCalledTimes(1)
  })
})

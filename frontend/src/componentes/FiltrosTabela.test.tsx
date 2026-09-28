import { act, fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { FILTROS_PADRAO } from '../filtros'
import { FiltrosTabela } from './FiltrosTabela'

describe('FiltrosTabela', () => {
  it('seleções chamam aoAlterar com o valor certo', async () => {
    const aoAlterar = vi.fn()
    render(<FiltrosTabela filtros={FILTROS_PADRAO} bancas={['Quadrix', 'IBGP']} aoAlterar={aoAlterar} />)

    await userEvent.selectOptions(screen.getByLabelText('UF'), 'SP')
    expect(aoAlterar).toHaveBeenLastCalledWith({ uf: 'SP' })
    await userEvent.selectOptions(screen.getByLabelText('UF'), 'NACIONAL')
    expect(aoAlterar).toHaveBeenLastCalledWith({ uf: 'NACIONAL' })
    await userEvent.selectOptions(screen.getByLabelText('Status'), 'PREVISTO')
    expect(aoAlterar).toHaveBeenLastCalledWith({ status: 'PREVISTO' })
    await userEvent.selectOptions(screen.getByLabelText('Banca'), 'IBGP')
    expect(aoAlterar).toHaveBeenLastCalledWith({ banca: 'IBGP' })
    await userEvent.selectOptions(screen.getByLabelText('Salário'), '8000')
    expect(aoAlterar).toHaveBeenLastCalledWith({ salarioMin: 8000 })
    await userEvent.selectOptions(screen.getByLabelText('Ordenar'), 'salarioMax,desc')
    expect(aoAlterar).toHaveBeenLastCalledWith({ sort: 'salarioMax,desc' })
  })

  it('opção "todas" limpa o filtro', async () => {
    const aoAlterar = vi.fn()
    render(<FiltrosTabela filtros={{ ...FILTROS_PADRAO, uf: 'SP', salarioMin: 8000 }} bancas={[]} aoAlterar={aoAlterar} />)
    await userEvent.selectOptions(screen.getByLabelText('UF'), '')
    expect(aoAlterar).toHaveBeenLastCalledWith({ uf: null })
    await userEvent.selectOptions(screen.getByLabelText('Salário'), '')
    expect(aoAlterar).toHaveBeenLastCalledWith({ salarioMin: null })
  })

  it('mantém a banca da URL como opção mesmo fora da lista', () => {
    render(<FiltrosTabela filtros={{ ...FILTROS_PADRAO, banca: 'Avança SP' }} bancas={['Quadrix']} aoAlterar={vi.fn()} />)
    expect(screen.getByLabelText('Banca')).toHaveValue('Avança SP')
  })

  it('a busca espera o usuário parar de digitar e não cria histórico', async () => {
    vi.useFakeTimers()
    const aoAlterar = vi.fn()
    render(<FiltrosTabela filtros={FILTROS_PADRAO} bancas={[]} aoAlterar={aoAlterar} />)

    fireEvent.change(screen.getByLabelText('Buscar órgão ou cargo'), { target: { value: 'analis' } })
    fireEvent.change(screen.getByLabelText('Buscar órgão ou cargo'), { target: { value: 'analista' } })
    await act(async () => {
      await vi.advanceTimersByTimeAsync(349)
    })
    expect(aoAlterar).not.toHaveBeenCalled()

    await act(async () => {
      await vi.advanceTimersByTimeAsync(1)
    })
    expect(aoAlterar).toHaveBeenCalledTimes(1)
    expect(aoAlterar).toHaveBeenCalledWith({ q: 'analista' }, { substituir: true })
  })
})

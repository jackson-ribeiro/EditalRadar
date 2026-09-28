import { act, renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useFiltrosUrl } from './useFiltrosUrl'

describe('useFiltrosUrl', () => {
  it('lê os filtros da URL atual', () => {
    window.history.replaceState(null, '', '/?uf=MG&page=2')
    const { result } = renderHook(() => useFiltrosUrl())
    expect(result.current.filtros.uf).toBe('MG')
    expect(result.current.filtros.page).toBe(2)
  })

  it('alterar grava na URL, cria histórico e volta para a página 0', () => {
    window.history.replaceState(null, '', '/?page=2')
    const tamanhoAntes = window.history.length
    const { result } = renderHook(() => useFiltrosUrl())

    act(() => result.current.alterar({ uf: 'SP' }))

    expect(result.current.filtros).toMatchObject({ uf: 'SP', page: 0 })
    expect(window.location.search).toBe('?uf=SP')
    expect(window.history.length).toBe(tamanhoAntes + 1)
  })

  it('alterar com substituir não cria histórico', () => {
    const { result } = renderHook(() => useFiltrosUrl())
    const tamanhoAntes = window.history.length

    act(() => result.current.alterar({ q: 'analista' }, { substituir: true }))

    expect(window.location.search).toBe('?q=analista')
    expect(window.history.length).toBe(tamanhoAntes)
  })

  it('irParaPagina e limpar', () => {
    window.history.replaceState(null, '', '/?uf=SP&sort=salarioMax%2Cdesc')
    const { result } = renderHook(() => useFiltrosUrl())

    act(() => result.current.irParaPagina(1))
    expect(window.location.search).toBe('?uf=SP&sort=salarioMax%2Cdesc&page=1')

    act(() => result.current.limpar())
    expect(window.location.search).toBe('?sort=salarioMax%2Cdesc')
    expect(result.current.filtros.uf).toBeNull()
  })

  it('acompanha o botão voltar do navegador', () => {
    const { result } = renderHook(() => useFiltrosUrl())
    act(() => {
      window.history.replaceState(null, '', '/?uf=RJ')
      window.dispatchEvent(new PopStateEvent('popstate'))
    })
    expect(result.current.filtros.uf).toBe('RJ')
  })
})

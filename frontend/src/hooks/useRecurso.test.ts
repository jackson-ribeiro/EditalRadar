import { act, renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { instalarFetch, respostaJson } from '../testes/apiFalsa'
import { useRecurso } from './useRecurso'

describe('useRecurso', () => {
  it('carrega os dados', async () => {
    instalarFetch(() => respostaJson({ abertos: 4 }))
    const { result } = renderHook(() => useRecurso<{ abertos: number }>('/api/dashboard/resumo'))

    expect(result.current.carregando).toBe(true)
    await waitFor(() => expect(result.current.carregando).toBe(false))
    expect(result.current.dados).toEqual({ abertos: 4 })
    expect(result.current.erro).toBeNull()
  })

  it('expõe o erro e recarrega sob demanda', async () => {
    let falhar = true
    const fetchFalso = instalarFetch(() =>
      falhar ? respostaJson({ detail: 'Falhou.' }, 400) : respostaJson({ abertos: 1 }),
    )
    const { result } = renderHook(() => useRecurso<{ abertos: number }>('/api/dashboard/resumo'))
    await waitFor(() => expect(result.current.erro?.message).toBe('Falhou.'))

    falhar = false
    act(() => result.current.recarregar())

    await waitFor(() => expect(result.current.dados).toEqual({ abertos: 1 }))
    expect(result.current.erro).toBeNull()
    expect(fetchFalso).toHaveBeenCalledTimes(2)
  })

  it('refaz a busca quando a URL ou a versão mudam e mantém os dados anteriores enquanto carrega', async () => {
    const fetchFalso = instalarFetch((url) => respostaJson({ url }))
    const { result, rerender } = renderHook(({ url, versao }) => useRecurso<{ url: string }>(url, versao), {
      initialProps: { url: '/api/concursos?page=0', versao: 0 },
    })
    await waitFor(() => expect(result.current.dados?.url).toBe('/api/concursos?page=0'))

    rerender({ url: '/api/concursos?page=1', versao: 0 })
    expect(result.current.dados?.url).toBe('/api/concursos?page=0')
    await waitFor(() => expect(result.current.dados?.url).toBe('/api/concursos?page=1'))

    rerender({ url: '/api/concursos?page=1', versao: 1 })
    await waitFor(() => expect(fetchFalso).toHaveBeenCalledTimes(3))
  })
})

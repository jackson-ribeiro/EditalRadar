import { act, renderHook } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Coleta } from '../api/tipos'
import { instalarFetch, respostaJson } from '../testes/apiFalsa'
import { useSincronizacao } from './useSincronizacao'

function coleta(status: Coleta['status']): Coleta {
  return {
    id: 1, status, origem: 'MANUAL', iniciadaEm: '2026-09-23T13:00:00Z',
    finalizadaEm: status === 'EM_ANDAMENTO' ? null : '2026-09-23T13:05:00Z',
    totalListagem: 457, novos: 2, atualizados: 1, encerrados: 0, descartados: 3,
    detalhesBaixados: 5, pendentes: 0, avisos: [], mensagemErro: status === 'FALHA' ? 'Listagem vazia' : null,
  }
}

async function avancar(ms: number) {
  await act(async () => {
    await vi.advanceTimersByTimeAsync(ms)
  })
}

describe('useSincronizacao', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  it('sem coleta anterior fica ocioso', async () => {
    instalarFetch(() => respostaJson(undefined, 204))
    const { result } = renderHook(() => useSincronizacao(vi.fn()))
    await avancar(0)

    expect(result.current.carregandoUltima).toBe(false)
    expect(result.current.ultima).toBeNull()
    expect(result.current.coletando).toBe(false)
  })

  it('ao abrir com coleta em andamento, acompanha até terminar', async () => {
    const respostas = [coleta('EM_ANDAMENTO'), coleta('EM_ANDAMENTO'), coleta('SUCESSO')]
    instalarFetch(() => respostaJson(respostas.length > 1 ? respostas.shift() : respostas[0]))
    const aoTerminar = vi.fn()
    const { result } = renderHook(() => useSincronizacao(aoTerminar))

    await avancar(0)
    expect(result.current.coletando).toBe(true)
    await avancar(3000)

    expect(result.current.coletando).toBe(false)
    expect(result.current.ultima?.status).toBe('SUCESSO')
    expect(aoTerminar).toHaveBeenCalledTimes(1)
  })

  it('atualizar dispara o sync e acompanha a coleta', async () => {
    const ultimas = [coleta('SUCESSO'), coleta('EM_ANDAMENTO'), coleta('SUCESSO')]
    const fetchFalso = instalarFetch((_url, init) =>
      init?.method === 'POST'
        ? respostaJson({ coletaId: 2 }, 202)
        : respostaJson(ultimas.length > 1 ? ultimas.shift() : ultimas[0]),
    )
    const aoTerminar = vi.fn()
    const { result } = renderHook(() => useSincronizacao(aoTerminar))
    await avancar(0)

    act(() => result.current.atualizar())
    await avancar(0)
    expect(result.current.coletando).toBe(true)
    await avancar(3000)

    expect(result.current.coletando).toBe(false)
    expect(aoTerminar).toHaveBeenCalledTimes(1)
    expect(fetchFalso).toHaveBeenCalledWith('/api/admin/sync', expect.objectContaining({ method: 'POST' }))
  })

  it('409 significa coleta já em andamento: acompanha em vez de mostrar erro', async () => {
    instalarFetch((_url, init) =>
      init?.method === 'POST'
        ? respostaJson({ title: 'Coleta em andamento', detail: 'Já existe uma coleta em andamento.', status: 409 }, 409)
        : respostaJson(coleta('EM_ANDAMENTO')),
    )
    const { result } = renderHook(() => useSincronizacao(vi.fn()))
    await avancar(0)

    act(() => result.current.atualizar())
    await avancar(0)

    expect(result.current.coletando).toBe(true)
    expect(result.current.erro).toBeNull()
  })

  it('falha de rede ao disparar mostra o erro e não entra em coleta', async () => {
    instalarFetch((_url, init) => {
      if (init?.method === 'POST') throw new TypeError('Failed to fetch')
      return respostaJson(coleta('SUCESSO'))
    })
    const { result } = renderHook(() => useSincronizacao(vi.fn()))
    await avancar(0)

    act(() => result.current.atualizar())
    await avancar(0)

    expect(result.current.coletando).toBe(false)
    expect(result.current.erro).toContain('Não foi possível falar com o servidor')
  })
})

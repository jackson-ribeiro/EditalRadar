import { describe, expect, it } from 'vitest'
import { instalarFetch, respostaJson } from '../testes/apiFalsa'
import { ErroApi, MENSAGEM_SEM_SERVIDOR, getJson, postJson } from './cliente'

describe('cliente da API', () => {
  it('retorna o JSON em caso de sucesso', async () => {
    instalarFetch(() => respostaJson({ abertos: 3 }))
    await expect(getJson('/api/dashboard/resumo')).resolves.toEqual({ abertos: 3 })
  })

  it('retorna null quando a resposta é 204', async () => {
    instalarFetch(() => respostaJson(undefined, 204))
    await expect(getJson('/api/admin/coletas/ultima')).resolves.toBeNull()
  })

  it('usa o detail do ProblemDetail como mensagem', async () => {
    instalarFetch(() => respostaJson({ title: 'Parâmetro inválido', detail: "UF inválida: 'XX'.", status: 400 }, 400))
    const erro = await getJson('/api/concursos').catch((e: unknown) => e)
    expect(erro).toBeInstanceOf(ErroApi)
    expect(erro).toMatchObject({ message: "UF inválida: 'XX'.", status: 400 })
  })

  it('explica quando o servidor não responde', async () => {
    instalarFetch(() => {
      throw new TypeError('Failed to fetch')
    })
    await expect(getJson('/api/concursos')).rejects.toMatchObject({ message: MENSAGEM_SEM_SERVIDOR, status: null })
  })

  it('trata 500 sem corpo (proxy do Vite sem backend) como servidor indisponível', async () => {
    instalarFetch(() => respostaJson(undefined, 500))
    await expect(getJson('/api/concursos')).rejects.toMatchObject({ message: MENSAGEM_SEM_SERVIDOR, status: 500 })
  })

  it('postJson envia POST', async () => {
    const fetchFalso = instalarFetch(() => respostaJson({ coletaId: 7 }, 202))
    await expect(postJson('/api/admin/sync')).resolves.toEqual({ coletaId: 7 })
    expect(fetchFalso).toHaveBeenCalledWith('/api/admin/sync', expect.objectContaining({ method: 'POST' }))
  })
})

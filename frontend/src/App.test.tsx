import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import App from './App'
import type { Concurso } from './api/tipos'
import { instalarFetch, respostaJson } from './testes/apiFalsa'

const CONCURSO: Concurso = {
  id: 1, urlOrigem: 'https://www.pciconcursos.com.br/noticias/camara-de-unai', titulo: null, orgao: 'Câmara de Unaí',
  cargo: 'Analista de Atividades da Secretaria', cargos: [], cargosTi: [], uf: 'MG', nacional: false, escolaridade: 'Superior', vagas: 2,
  cadastroReserva: true, salarioMin: null, salarioMax: 9193.89, trechoRemuneracao: null, trechoTaxa: null, taxaInscricao: 80, banca: 'Consulplan', status: 'ABERTO', inicioInscricao: '2026-10-30',
  fimInscricao: '2026-12-01', dataProva: '2027-01-10', origemTi: 'MCP', primeiraVezVistoEm: '2026-09-22T10:00:00Z',
  atualizadoEm: '2026-09-22T10:00:00Z', novo: true, diasRestantes: 69,
}

function apiNormal(url: string, init?: RequestInit) {
  if (init?.method === 'POST') return respostaJson({ coletaId: 9 }, 202)
  if (url.startsWith('/api/concursos')) {
    return respostaJson({ content: [CONCURSO], page: 0, size: 20, totalElements: 1, totalPages: 1 })
  }
  if (url === '/api/dashboard/resumo') {
    return respostaJson({ abertos: 1, previstos: 0, encerrandoEm7Dias: 1, novosNaSemana: 1, ultimaColeta: null })
  }
  if (url === '/api/dashboard/por-uf') return respostaJson([{ uf: 'MG', total: 1 }, { uf: 'SP', total: 5 }])
  if (url === '/api/dashboard/por-banca') return respostaJson([{ banca: 'Consulplan', total: 1 }])
  if (url === '/api/dashboard/por-faixa-salarial') return respostaJson([{ faixa: 'R$ 8–12 mil', total: 1 }])
  if (url === '/api/admin/coletas/ultima') return respostaJson(undefined, 204)
  return respostaJson({ detail: `Rota inesperada: ${url}` }, 404)
}

describe('App', () => {
  it('monta a página com frase, faixa e tabela', async () => {
    instalarFetch(apiNormal)
    render(<App />)

    expect(await screen.findByRole('link', { name: 'Câmara de Unaí' })).toBeInTheDocument()
    expect(screen.getByText('1 concurso de TI')).toBeInTheDocument()
    expect(screen.getByText('encerra em 7 dias')).toBeInTheDocument()
    expect(screen.getByText('Ainda não houve coleta')).toBeInTheDocument()
  })

  it('clicar numa UF do gráfico filtra a tabela e grava na URL', async () => {
    const fetchFalso = instalarFetch(apiNormal)
    render(<App />)

    await userEvent.click(await screen.findByRole('button', { name: 'SP: 5' }))

    expect(window.location.search).toBe('?uf=SP')
    await waitFor(() =>
      expect(fetchFalso.mock.calls.some(([url]) => String(url).startsWith('/api/concursos') && String(url).includes('uf=SP'))).toBe(true),
    )
  })

  it('trocar o filtro fecha os resumos abertos', async () => {
    instalarFetch(apiNormal)
    render(<App />)

    await userEvent.click(await screen.findByRole('button', { name: 'Ver resumo de Câmara de Unaí' }))
    expect(screen.getByText('Taxa de inscrição')).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'SP: 5' }))

    await waitFor(() => expect(screen.queryByText('Taxa de inscrição')).not.toBeInTheDocument())
  })

  it('Atualizar agora dispara o sync', async () => {
    const fetchFalso = instalarFetch(apiNormal)
    render(<App />)

    await userEvent.click(await screen.findByRole('button', { name: 'Atualizar agora' }))

    await waitFor(() => expect(fetchFalso).toHaveBeenCalledWith('/api/admin/sync', expect.objectContaining({ method: 'POST' })))
  })

  it('página além do total volta para a última página em vez de mostrar vazio', async () => {
    window.history.replaceState(null, '', '/?page=2')
    instalarFetch((url, init) => {
      if (url.startsWith('/api/concursos') && url.includes('page=2')) {
        return respostaJson({ content: [], page: 2, size: 20, totalElements: 45, totalPages: 2 })
      }
      return apiNormal(url, init)
    })
    render(<App />)

    await waitFor(() => expect(window.location.search).toBe('?page=1'))
    expect(await screen.findByRole('link', { name: 'Câmara de Unaí' })).toBeInTheDocument()
    expect(screen.queryByText(/por enquanto/)).not.toBeInTheDocument()
  })

  it('com o backend fora do ar, cada bloco mostra seu erro e a página continua de pé', async () => {
    instalarFetch(() => respostaJson(undefined, 500))
    render(<App />)

    await waitFor(() => expect(screen.getAllByRole('alert').length).toBeGreaterThanOrEqual(4))
    expect(screen.getAllByRole('button', { name: 'Tentar de novo' }).length).toBeGreaterThanOrEqual(4)
    expect(screen.getByRole('heading', { name: 'EditalRadar' })).toBeInTheDocument()
  })
})

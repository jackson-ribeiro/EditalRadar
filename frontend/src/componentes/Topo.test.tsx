import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ErroApi } from '../api/cliente'
import type { Coleta, Resumo } from '../api/tipos'
import type { EstadoSincronizacao } from '../hooks/useSincronizacao'
import { Cabecalho } from './Cabecalho'
import { FaixaResumo } from './FaixaResumo'
import { FraseDestaque } from './FraseDestaque'

const AGORA = new Date('2026-09-23T13:00:00Z')

function coleta(parcial: Partial<Coleta>): Coleta {
  return {
    id: 1, status: 'SUCESSO', origem: 'AGENDADA', iniciadaEm: '2026-09-23T10:00:00Z', finalizadaEm: '2026-09-23T10:02:00Z',
    totalListagem: 457, novos: 2, atualizados: 1, encerrados: 0, descartados: 3, detalhesBaixados: 5, pendentes: 0,
    avisos: [], mensagemErro: null, ...parcial,
  }
}

function sync(parcial: Partial<EstadoSincronizacao>): EstadoSincronizacao {
  return { ultima: null, carregandoUltima: false, coletando: false, erro: null, atualizar: vi.fn(), ...parcial }
}

const RESUMO: Resumo = { abertos: 42, previstos: 1, encerrandoEm7Dias: 3, novosNaSemana: 5, ultimaColeta: null }

describe('Cabecalho', () => {
  it('mostra a última coleta e dispara a atualização', async () => {
    const estado = sync({ ultima: coleta({}) })
    render(<Cabecalho sync={estado} agora={AGORA} />)
    expect(screen.getByRole('heading', { name: 'EditalRadar' })).toBeInTheDocument()
    expect(screen.getByText('Coletado hoje às 07:02')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Atualizar agora' }))
    expect(estado.atualizar).toHaveBeenCalledTimes(1)
  })

  it('coleta recém-terminada aparece como "Coletado agora"', () => {
    render(<Cabecalho sync={sync({ ultima: coleta({ finalizadaEm: '2026-09-23T12:59:40Z' }) })} agora={AGORA} />)
    expect(screen.getByText('Coletado agora')).toBeInTheDocument()
  })

  it('sem coleta anterior', () => {
    render(<Cabecalho sync={sync({})} agora={AGORA} />)
    expect(screen.getByText('Ainda não houve coleta')).toBeInTheDocument()
  })

  it('coletando desabilita o botão e mostra há quanto tempo', () => {
    render(<Cabecalho sync={sync({ coletando: true, ultima: coleta({ status: 'EM_ANDAMENTO', iniciadaEm: '2026-09-23T12:58:00Z', finalizadaEm: null }) })} agora={AGORA} />)
    expect(screen.getByRole('button', { name: 'Coletando…' })).toBeDisabled()
    expect(screen.getByText('Coletando… iniciada há 2 min')).toBeInTheDocument()
  })

  it('falha da última coleta vira "Tentar de novo"', () => {
    render(<Cabecalho sync={sync({ ultima: coleta({ status: 'FALHA', mensagemErro: 'Listagem vazia' }) })} agora={AGORA} />)
    expect(screen.getByText('A última coleta falhou: Listagem vazia')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeEnabled()
  })

  it('erro ao disparar aparece como alerta', () => {
    render(<Cabecalho sync={sync({ ultima: coleta({}), erro: 'O servidor caiu.' })} agora={AGORA} />)
    expect(screen.getByRole('alert')).toHaveTextContent('O servidor caiu.')
  })
})

describe('FraseDestaque', () => {
  it('destaca a quantidade que encerra na semana', () => {
    const { container } = render(<FraseDestaque resumo={RESUMO} />)
    expect(screen.getByText('3 concursos de TI')).toHaveClass('text-vencendo')
    expect(container.querySelector('p')).toHaveTextContent('3 concursos de TI encerram as inscrições nos próximos 7 dias.')
  })

  it('sem nada encerrando', () => {
    render(<FraseDestaque resumo={{ ...RESUMO, encerrandoEm7Dias: 0 }} />)
    expect(screen.getByText('Nenhum concurso de TI encerra as inscrições nos próximos 7 dias.')).toBeInTheDocument()
  })

  it('sem resumo não mostra nada', () => {
    const { container } = render(<FraseDestaque resumo={null} />)
    expect(container).toBeEmptyDOMElement()
  })
})

describe('FaixaResumo', () => {
  it('mostra os quatro números com rótulos no singular ou plural', () => {
    render(<FaixaResumo recurso={{ dados: RESUMO, carregando: false, erro: null, recarregar: vi.fn() }} />)
    expect(screen.getByText('42')).toBeInTheDocument()
    expect(screen.getByText('abertos')).toBeInTheDocument()
    expect(screen.getByText('previsto')).toBeInTheDocument()
    expect(screen.getByText('encerram em 7 dias')).toBeInTheDocument()
    expect(screen.getByText('novos na semana')).toBeInTheDocument()
  })

  it('erro com tentar de novo', async () => {
    const recarregar = vi.fn()
    render(<FaixaResumo recurso={{ dados: null, carregando: false, erro: new ErroApi('Falhou.', 500), recarregar }} />)
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))
    expect(recarregar).toHaveBeenCalledTimes(1)
  })
})

import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import type { Concurso } from '../api/tipos'
import { PainelResumo } from './PainelResumo'

function concurso(parcial: Partial<Concurso>): Concurso {
  return {
    id: 7, urlOrigem: 'https://www.pciconcursos.com.br/noticias/dpe-pb', titulo: null, orgao: 'Defensoria Pública da Paraíba',
    cargo: 'Vários Cargos', cargos: [], cargosTi: [], uf: 'PB', nacional: false, escolaridade: 'Médio / Superior', vagas: 20,
    cadastroReserva: false, salarioMin: null, salarioMax: 5197.5, trechoRemuneracao: null, trechoTaxa: null, taxaInscricao: null,
    banca: 'FCC', status: 'ABERTO', inicioInscricao: '2026-09-28', fimInscricao: '2026-11-13', dataProva: '2027-02-28',
    origemTi: 'DETALHE', primeiraVezVistoEm: '2026-09-22T10:00:00Z', atualizadoEm: '2026-09-22T10:00:00Z', novo: true,
    diasRestantes: 51, ...parcial,
  }
}

function valorDe(rotulo: string) {
  return screen.getByText(rotulo, { selector: 'dt' }).nextElementSibling as HTMLElement
}

describe('PainelResumo', () => {
  it('vários cargos: cargos de TI, trechos e datas', () => {
    render(
      <PainelResumo
        id="resumo-7"
        concurso={concurso({
          cargos: ['Técnico (9 vagas)', 'Analista - Desenvolvimento de Sistemas (1 vaga)', 'Analista - Segurança da Informação (1 vaga)', 'Analista - Psicólogo (1 vaga)'],
          cargosTi: ['Analista - Desenvolvimento de Sistemas (1 vaga)', 'Analista - Segurança da Informação (1 vaga)'],
          trechoRemuneracao: 'A remuneração para Técnico é de R$ 4.042,50 e para Analista é de R$ 5.197,50.',
          trechoTaxa: 'As taxas de inscrição são de R$ 103,00 para cargos de nível médio e de R$ 123,00 para cargos de nível superior.',
        })}
      />,
    )

    expect(document.getElementById('resumo-7')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 3, name: 'Cargos de TI' })).toBeInTheDocument()
    expect(screen.getByText('Analista - Segurança da Informação (1 vaga)')).toBeInTheDocument()
    expect(screen.getByText('e mais 2 cargos de outras áreas')).toBeInTheDocument()
    expect(valorDe('Salário')).toHaveTextContent('A remuneração para Técnico é de R$ 4.042,50 e para Analista é de R$ 5.197,50.trecho da notícia')
    expect(valorDe('Taxa de inscrição')).toHaveTextContent('trecho da notícia')
    expect(valorDe('Escolaridade')).toHaveTextContent('Médio / Superior')
    expect(valorDe('Inscrições')).toHaveTextContent('28/09/2026 a 13/11/2026')
    expect(valorDe('Prova')).toHaveTextContent('28/02/2027')
    expect(valorDe('Banca')).toHaveTextContent('FCC')
    expect(screen.getByRole('link', { name: 'Abrir notícia no PCI' })).toHaveAttribute('href', 'https://www.pciconcursos.com.br/noticias/dpe-pb')
  })

  it('cargo único: salário e taxa como valores', () => {
    render(
      <PainelResumo
        id="resumo-8"
        concurso={concurso({
          cargo: 'Analista de Sistemas Júnior', cargos: ['Analista de Sistemas Júnior (1 vaga)'],
          cargosTi: ['Analista de Sistemas Júnior (1 vaga)'], salarioMax: 5100, taxaInscricao: 90,
          trechoTaxa: 'A taxa de inscrição é de R$ 90,00.', escolaridade: null, banca: null, dataProva: null, inicioInscricao: null,
        })}
      />,
    )

    expect(screen.getByRole('heading', { name: 'Cargo de TI' })).toBeInTheDocument()
    expect(screen.queryByText(/outras áreas|outra área/)).not.toBeInTheDocument()
    expect(valorDe('Salário')).toHaveTextContent('R$ 5.100,00')
    expect(valorDe('Salário')).not.toHaveTextContent('trecho da notícia')
    expect(valorDe('Taxa de inscrição')).toHaveTextContent('R$ 90,00')
    expect(valorDe('Escolaridade')).toHaveTextContent('não informada')
    expect(valorDe('Inscrições')).toHaveTextContent('até 13/11/2026')
    expect(valorDe('Prova')).toHaveTextContent('não informada na notícia')
    expect(valorDe('Banca')).toHaveTextContent('não identificada')
  })

  it('sem trechos: máximo da listagem e taxa não informada', () => {
    render(<PainelResumo id="resumo-9" concurso={concurso({ cargos: ['A', 'B'] })} />)

    expect(screen.getByRole('heading', { name: 'Cargos' })).toBeInTheDocument()
    expect(valorDe('Salário')).toHaveTextContent('até R$ 5.197,50 (valor máximo da listagem)')
    expect(valorDe('Taxa de inscrição')).toHaveTextContent('não informada na notícia')
  })

  it('cargos com texto repetido aparecem todos, sem aviso de chave duplicada', () => {
    const erro = vi.spyOn(console, 'error').mockImplementation(() => {})
    render(<PainelResumo id="resumo-10" concurso={concurso({ cargos: ['Analista (1 vaga)', 'Analista (1 vaga)'], cargosTi: ['Analista (1 vaga)', 'Analista (1 vaga)'] })} />)

    expect(screen.getAllByText('Analista (1 vaga)')).toHaveLength(2)
    expect(erro).not.toHaveBeenCalled()
    erro.mockRestore()
  })

  it('no celular os rótulos ficam acima dos valores', () => {
    render(<PainelResumo id="resumo-11" concurso={concurso({})} />)
    const lista = screen.getByText('Salário', { selector: 'dt' }).closest('dl')
    expect(lista).toHaveClass('grid-cols-1', 'sm:grid-cols-[8.5rem_1fr]')
  })
})

import { fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ErroApi } from '../api/cliente'
import type { Concurso, Pagina } from '../api/tipos'
import { TabelaConcursos } from './TabelaConcursos'

function concurso(parcial: Partial<Concurso>): Concurso {
  return {
    id: 1, urlOrigem: 'https://www.pciconcursos.com.br/noticias/x', titulo: null, orgao: 'Órgão', cargo: 'Analista de TI',
    cargos: [], cargosTi: [], uf: 'SP', nacional: false, escolaridade: 'Superior', vagas: 1, cadastroReserva: false,
    salarioMin: null, salarioMax: 5000, trechoRemuneracao: null, trechoTaxa: null, taxaInscricao: null, banca: 'Quadrix', status: 'ABERTO', inicioInscricao: null,
    fimInscricao: '2026-10-21', dataProva: null, origemTi: 'LISTAGEM', primeiraVezVistoEm: '2026-09-01T10:00:00Z',
    atualizadoEm: '2026-09-01T10:00:00Z', novo: false, diasRestantes: 28, ...parcial,
  }
}

function pagina(content: Concurso[], extra: Partial<Pagina<Concurso>> = {}): Pagina<Concurso> {
  return { content, page: 0, size: 20, totalElements: content.length, totalPages: 1, ...extra }
}

const acoes = () => ({ aoTentarDeNovo: vi.fn(), aoLimparFiltros: vi.fn(), aoMudarPagina: vi.fn() })

describe('TabelaConcursos', () => {
  it('lista concursos com link, novo, nacional e cargo descrito', () => {
    render(
      <TabelaConcursos
        pagina={pagina([
          concurso({ id: 1, orgao: 'Prefeitura de Contagem', urlOrigem: 'https://www.pciconcursos.com.br/noticias/contagem',
            novo: true, cargo: 'Vários Cargos', cargos: ['Assistente', 'Analista de TI (1 vaga)'], cargosTi: ['Analista de TI (1 vaga)'], uf: 'MG' }),
          concurso({ id: 2, orgao: 'CRBio-01', uf: null, nacional: true, salarioMax: 7181.5 }),
        ])}
        carregando={false} erro={null} status="ABERTO" filtroAtivo={false} {...acoes()}
      />,
    )

    const link = screen.getByRole('link', { name: 'Prefeitura de Contagem' })
    expect(link).toHaveAttribute('href', 'https://www.pciconcursos.com.br/noticias/contagem')
    expect(link).toHaveAttribute('target', '_blank')
    const linhaContagem = link.closest('tr')!
    expect(within(linhaContagem).getByText('novo')).toBeInTheDocument()
    expect(within(linhaContagem).getByText('Analista de TI (1 vaga)')).toHaveAttribute('title', 'Analista de TI (1 vaga)\nAssistente')
    expect(within(linhaContagem).getByText('+1 outro cargo')).toBeInTheDocument()
    const linhaCrbio = screen.getByRole('link', { name: 'CRBio-01' }).closest('tr')!
    expect(within(linhaCrbio).getByText('Nacional')).toBeInTheDocument()
    expect(within(linhaCrbio).getByText('R$ 7.181,50')).toBeInTheDocument()
  })

  it('concurso sem data, salário e banca mostra traços e "sem data"', () => {
    render(
      <TabelaConcursos
        pagina={pagina([concurso({ banca: null, salarioMax: null, fimInscricao: null, diasRestantes: null,
          vagas: null, cadastroReserva: true })])}
        carregando={false} erro={null} status="ABERTO" filtroAtivo={false} {...acoes()}
      />,
    )
    const linha = screen.getByRole('link', { name: 'Órgão' }).closest('tr')!
    expect(within(linha).getAllByText('—')).toHaveLength(2)
    expect(within(linha).getByText('cadastro reserva')).toBeInTheDocument()
    expect(within(linha).getByText('sem data')).toBeInTheDocument()
  })

  it('carregando sem dados anteriores mostra o esqueleto', () => {
    render(<TabelaConcursos pagina={null} carregando erro={null} status="ABERTO" filtroAtivo={false} {...acoes()} />)
    expect(screen.getByRole('status', { name: 'Carregando concursos' })).toBeInTheDocument()
  })

  it('vazio com filtro oferece limpar filtros', async () => {
    const funcoes = acoes()
    render(<TabelaConcursos pagina={pagina([])} carregando={false} erro={null} status="ABERTO" filtroAtivo {...funcoes} />)
    expect(screen.getByText('Nenhum concurso de TI aberto com esses filtros.')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Limpar filtros' }))
    expect(funcoes.aoLimparFiltros).toHaveBeenCalledTimes(1)
  })

  it('vazio sem filtro sugere atualizar', () => {
    render(<TabelaConcursos pagina={pagina([])} carregando={false} erro={null} status="PREVISTO" filtroAtivo={false} {...acoes()} />)
    expect(
      screen.getByText('Nenhum concurso de TI previsto por enquanto. Clique em Atualizar agora para coletar.'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Limpar filtros' })).not.toBeInTheDocument()
  })

  it('erro mostra a mensagem e tenta de novo', async () => {
    const funcoes = acoes()
    render(
      <TabelaConcursos pagina={null} carregando={false} erro={new ErroApi('O servidor caiu.', 500)}
        status="ABERTO" filtroAtivo={false} {...funcoes} />,
    )
    expect(screen.getByRole('alert')).toHaveTextContent('O servidor caiu.')
    await userEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))
    expect(funcoes.aoTentarDeNovo).toHaveBeenCalledTimes(1)
  })

  it('pagina entre resultados', async () => {
    const funcoes = acoes()
    render(
      <TabelaConcursos pagina={pagina([concurso({})], { totalElements: 45, totalPages: 3 })} carregando={false}
        erro={null} status="ABERTO" filtroAtivo={false} {...funcoes} />,
    )
    expect(screen.getByText('Página 1 de 3')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: '‹ Anterior' })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: 'Próxima ›' }))
    expect(funcoes.aoMudarPagina).toHaveBeenCalledWith(1)
  })

  it('abre e fecha o resumo pela seta', async () => {
    render(<TabelaConcursos pagina={pagina([concurso({ orgao: 'Câmara de Unaí' })])} carregando={false} erro={null}
      status="ABERTO" filtroAtivo={false} {...acoes()} />)

    const seta = screen.getByRole('button', { name: 'Ver resumo de Câmara de Unaí' })
    expect(seta).toHaveAttribute('aria-expanded', 'false')
    expect(seta).not.toHaveAttribute('aria-controls')
    await userEvent.click(seta)

    expect(screen.getByRole('button', { name: 'Ocultar resumo de Câmara de Unaí' })).toHaveAttribute('aria-expanded', 'true')
    expect(screen.getByText('Taxa de inscrição')).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Ocultar resumo de Câmara de Unaí' }))
    expect(screen.queryByText('Taxa de inscrição')).not.toBeInTheDocument()
  })

  it('clicar na linha abre; clicar no link do órgão não abre', async () => {
    render(<TabelaConcursos pagina={pagina([concurso({ orgao: 'Câmara de Unaí', banca: 'Consulplan' })])} carregando={false}
      erro={null} status="ABERTO" filtroAtivo={false} {...acoes()} />)

    fireEvent.click(screen.getByRole('link', { name: 'Câmara de Unaí' }))
    expect(screen.queryByText('Taxa de inscrição')).not.toBeInTheDocument()

    await userEvent.click(screen.getByText('Consulplan'))
    expect(screen.getByText('Taxa de inscrição')).toBeInTheDocument()
  })

  it('permite vários resumos abertos ao mesmo tempo', async () => {
    render(<TabelaConcursos pagina={pagina([concurso({ id: 1, orgao: 'Órgão A' }), concurso({ id: 2, orgao: 'Órgão B' })])}
      carregando={false} erro={null} status="ABERTO" filtroAtivo={false} {...acoes()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Ver resumo de Órgão A' }))
    await userEvent.click(screen.getByRole('button', { name: 'Ver resumo de Órgão B' }))

    expect(screen.getAllByText('Taxa de inscrição')).toHaveLength(2)
    expect(screen.getByRole('button', { name: 'Ocultar resumo de Órgão A' })).toHaveAttribute('aria-controls', 'resumo-1')
  })

  it('selecionar texto na linha não abre o resumo', async () => {
    render(<TabelaConcursos pagina={pagina([concurso({ orgao: 'Câmara de Unaí', banca: 'Consulplan' })])} carregando={false}
      erro={null} status="ABERTO" filtroAtivo={false} {...acoes()} />)
    const selecao = vi.spyOn(window, 'getSelection').mockReturnValue({ toString: () => 'Consulplan' } as Selection)

    fireEvent.click(screen.getByText('Consulplan'))

    expect(screen.queryByText('Taxa de inscrição')).not.toBeInTheDocument()
    selecao.mockRestore()
  })
})

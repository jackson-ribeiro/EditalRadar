import { describe, expect, it } from 'vitest'
import { FILTROS_PADRAO, escreverFiltros, lerFiltros, limparFiltros, paramsDaApi, temFiltroAtivo } from './filtros'

describe('filtros', () => {
  it('lê filtros válidos da URL', () => {
    expect(lerFiltros('?q=analista&uf=sp&status=PREVISTO&banca=Quadrix&salarioMin=8000&sort=salarioMax,desc&page=2')).toEqual({
      q: 'analista',
      uf: 'SP',
      status: 'PREVISTO',
      banca: 'Quadrix',
      salarioMin: 8000,
      sort: 'salarioMax,desc',
      page: 2,
    })
  })

  it('ignora valores inválidos e usa o padrão', () => {
    expect(lerFiltros('?uf=XX&status=FOO&page=-3&salarioMin=123&sort=orgao,asc')).toEqual(FILTROS_PADRAO)
    expect(lerFiltros('')).toEqual(FILTROS_PADRAO)
  })

  it('página absurda na URL volta para a primeira', () => {
    expect(lerFiltros('?page=99999999999').page).toBe(0)
    expect(lerFiltros('?page=1.5').page).toBe(0)
  })

  it('aceita NACIONAL como UF', () => {
    expect(lerFiltros('?uf=nacional').uf).toBe('NACIONAL')
  })

  it('escreve só o que difere do padrão', () => {
    expect(escreverFiltros(FILTROS_PADRAO)).toBe('')
    expect(escreverFiltros({ ...FILTROS_PADRAO, uf: 'SP', page: 1 })).toBe('?uf=SP&page=1')
  })

  it('preserva banca com acento e espaço na ida e volta', () => {
    const filtros = { ...FILTROS_PADRAO, banca: 'Avança SP' }
    expect(lerFiltros(escreverFiltros(filtros))).toEqual(filtros)
  })

  it('monta os parâmetros da API sempre com status, página, tamanho e ordenação', () => {
    const params = new URLSearchParams(paramsDaApi({ ...FILTROS_PADRAO, q: 'ti', salarioMin: 5000 }))
    expect(Object.fromEntries(params)).toEqual({
      status: 'ABERTO',
      page: '0',
      size: '20',
      sort: 'fimInscricao,asc',
      q: 'ti',
      salarioMin: '5000',
    })
  })

  it('detecta filtro ativo e limpa mantendo a ordenação', () => {
    expect(temFiltroAtivo(FILTROS_PADRAO)).toBe(false)
    expect(temFiltroAtivo({ ...FILTROS_PADRAO, status: 'ENCERRADO' })).toBe(true)
    const filtrado = { ...FILTROS_PADRAO, uf: 'SP', sort: 'salarioMax,desc' as const, page: 3 }
    expect(limparFiltros(filtrado)).toEqual({ ...FILTROS_PADRAO, sort: 'salarioMax,desc' })
  })
})

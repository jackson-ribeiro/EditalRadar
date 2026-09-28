import { describe, expect, it } from 'vitest'
import {
  descreverCargo,
  listaCargos,
  descreverDuracao,
  descreverMomento,
  formatarData,
  formatarMoeda,
  fraseDestaque,
  larguraRegua,
  minutosDesde,
  nivelUrgencia,
  plural,
  textoDias,
  descreverInscricoes,
  ehCargoUnico,
  resumoCargos,
  resumoSalario,
  resumoTaxa,
  textoOutrosCargos,
} from './formatos'

const AGORA = new Date('2026-09-23T13:00:00Z') // 10:00 em São Paulo

describe('formatos', () => {
  it('formata moeda em reais', () => {
    expect(formatarMoeda(13305.57)).toBe('R$ 13.305,57')
    expect(formatarMoeda(null)).toBe('—')
  })

  it('formata data ISO sem depender do fuso', () => {
    expect(formatarData('2026-12-01')).toBe('01/12/2026')
    expect(formatarData(null)).toBe('—')
  })

  it('descreve o momento da coleta no fuso de São Paulo', () => {
    expect(descreverMomento('2026-09-23T10:02:00Z', AGORA)).toBe('hoje às 07:02')
    expect(descreverMomento('2026-09-22T10:02:00Z', AGORA)).toBe('ontem às 07:02')
    expect(descreverMomento('2026-09-20T22:30:00Z', AGORA)).toBe('em 20/09 às 19:30')
    expect(descreverMomento('2026-09-23T02:30:00Z', AGORA)).toBe('ontem às 23:30')
  })

  it('calcula e descreve minutos decorridos', () => {
    expect(minutosDesde('2026-09-23T12:58:10Z', AGORA)).toBe(1)
    expect(minutosDesde('2026-09-23T13:05:00Z', AGORA)).toBe(0)
    expect(descreverDuracao(0)).toBe('menos de 1 min')
    expect(descreverDuracao(1)).toBe('1 min')
    expect(descreverDuracao(12)).toBe('12 min')
  })

  it('classifica a urgência nos limites', () => {
    expect(nivelUrgencia(null)).toBe('sem-data')
    expect(nivelUrgencia(-1)).toBe('encerrado')
    expect(nivelUrgencia(0)).toBe('vencendo')
    expect(nivelUrgencia(7)).toBe('vencendo')
    expect(nivelUrgencia(8)).toBe('prazo')
    expect(nivelUrgencia(14)).toBe('prazo')
    expect(nivelUrgencia(15)).toBe('tranquilo')
  })

  it('escreve os dias restantes', () => {
    expect(textoDias(null)).toBe('sem data')
    expect(textoDias(-2)).toBe('encerrado')
    expect(textoDias(0)).toBe('encerra hoje')
    expect(textoDias(1)).toBe('1 dia')
    expect(textoDias(28)).toBe('28 dias')
  })

  it('calcula a largura da régua com mínimo visível', () => {
    expect(larguraRegua(0)).toBe(4)
    expect(larguraRegua(2)).toBe(4)
    expect(larguraRegua(30)).toBe(50)
    expect(larguraRegua(60)).toBe(100)
    expect(larguraRegua(90)).toBe(100)
  })

  it('monta a frase de destaque', () => {
    expect(fraseDestaque(0)).toEqual({
      destaque: null,
      resto: 'Nenhum concurso de TI encerra as inscrições nos próximos 7 dias.',
    })
    expect(fraseDestaque(1)).toEqual({
      destaque: '1 concurso de TI',
      resto: ' encerra as inscrições nos próximos 7 dias.',
    })
    expect(fraseDestaque(3)).toEqual({
      destaque: '3 concursos de TI',
      resto: ' encerram as inscrições nos próximos 7 dias.',
    })
  })

  it('pluraliza', () => {
    expect(plural(1, 'concurso', 'concursos')).toBe('1 concurso')
    expect(plural(12, 'concurso', 'concursos')).toBe('12 concursos')
    expect(plural(0, 'concurso', 'concursos')).toBe('0 concursos')
  })

  it('descreve o cargo sem assumir que o primeiro é de TI', () => {
    const cargo = (c: string | null, cargos: string[], cargosTi: string[] = []) => descreverCargo({ cargo: c, cargos, cargosTi })
    expect(cargo('Analista de Sistemas', ['A', 'B'])).toEqual({ principal: 'Analista de Sistemas', complemento: null })
    expect(cargo('Vários Cargos', ['Analista de TI (1 vaga)'])).toEqual({ principal: 'Analista de TI (1 vaga)', complemento: null })
    expect(cargo('Vários Cargos', ['Assistente', 'Analista de TI', 'Arquiteto'])).toEqual({ principal: 'Vários cargos (3)', complemento: null })
    expect(cargo('Vários Cargos', [])).toEqual({ principal: 'Vários Cargos', complemento: null })
    expect(cargo(null, [])).toEqual({ principal: 'Cargo não informado', complemento: null })
  })

  it('destaca o cargo de TI e resume os demais', () => {
    const cargo = (c: string | null, cargos: string[], cargosTi: string[]) => descreverCargo({ cargo: c, cargos, cargosTi })
    const outros = Array.from({ length: 20 }, (_, i) => `Cargo ${i}`)
    expect(cargo('Vários Cargos', [...outros, 'Ciência da Computação'], ['Ciência da Computação']))
      .toEqual({ principal: 'Ciência da Computação', complemento: '+20 outros cargos' })
    expect(cargo('Vários Cargos', ['Assistente', 'Técnico em TI'], ['Técnico em TI']))
      .toEqual({ principal: 'Técnico em TI', complemento: '+1 outro cargo' })
    expect(cargo('Vários Cargos', ['Técnico em TI', 'Programador', 'Analista de TI'], ['Técnico em TI', 'Programador', 'Analista de TI']))
      .toEqual({ principal: 'Técnico em TI', complemento: '+2 de TI' })
    expect(cargo('Vários Cargos', ['A', 'Técnico em TI', 'Programador', 'B', 'Analista de TI'], ['Técnico em TI', 'Programador', 'Analista de TI']))
      .toEqual({ principal: 'Técnico em TI', complemento: '+2 de TI · +2 outros' })
  })

  it('prefere o cargo de TI ao cargo específico da listagem', () => {
    expect(descreverCargo({
      cargo: 'Analista Previdenciário, Técnico Previdenciário',
      cargos: ['Analista Previdenciário - Direito', 'Analista Previdenciário - Tecnologia da Informação (2 vagas + CR)'],
      cargosTi: ['Analista Previdenciário - Tecnologia da Informação (2 vagas + CR)'],
    })).toEqual({ principal: 'Analista Previdenciário - Tecnologia da Informação (2 vagas + CR)', complemento: '+1 outro cargo' })
  })

  it('mantém o cargo da listagem quando ele é o único', () => {
    expect(descreverCargo({ cargo: 'Analista de Sistemas Júnior', cargos: ['Analista de Sistemas Júnior (1 vaga)'],
      cargosTi: ['Analista de Sistemas Júnior (1 vaga)'] })).toEqual({ principal: 'Analista de Sistemas Júnior', complemento: null })
  })

  it('lista os cargos de TI primeiro no detalhe da linha', () => {
    expect(listaCargos({ cargos: ['Assistente', 'Analista de TI', 'Arquiteto'], cargosTi: ['Analista de TI'] }))
      .toBe('Analista de TI\nAssistente\nArquiteto')
    expect(listaCargos({ cargos: ['Analista de TI'], cargosTi: ['Analista de TI'] })).toBeUndefined()
  })
})

type BaseResumo = Parameters<typeof resumoSalario>[0]

function base(parcial: Partial<BaseResumo>): BaseResumo {
  return {
    cargo: 'Analista de Sistemas', cargos: [], cargosTi: [], salarioMax: null,
    trechoRemuneracao: null, trechoTaxa: null, taxaInscricao: null, ...parcial,
  }
}

describe('resumo do concurso', () => {
  it('cargo único', () => {
    expect(ehCargoUnico(base({}))).toBe(true)
    expect(ehCargoUnico(base({ cargos: ['Analista de Sistemas Júnior (1 vaga)'] }))).toBe(true)
    expect(ehCargoUnico(base({ cargo: 'Vários Cargos', cargos: ['Analista de TI (1 vaga)'] }))).toBe(false)
    expect(ehCargoUnico(base({ cargos: ['A', 'B'] }))).toBe(false)
  })

  it('salário: valor, trecho, máximo e ausente', () => {
    expect(resumoSalario(base({ salarioMax: 5100 }))).toEqual({ tipo: 'valor', valor: 5100 })
    expect(resumoSalario(base({ cargo: 'Vários Cargos', cargos: ['A', 'B'], salarioMax: 5197.5, trechoRemuneracao: 'Para Analista é de R$ 5.197,50.' })))
      .toEqual({ tipo: 'trecho', texto: 'Para Analista é de R$ 5.197,50.' })
    expect(resumoSalario(base({ cargo: 'Vários Cargos', cargos: ['A', 'B'], salarioMax: 7000 }))).toEqual({ tipo: 'maximo', valor: 7000 })
    expect(resumoSalario(base({ cargo: 'Vários Cargos', cargos: ['A', 'B'] }))).toEqual({ tipo: 'ausente' })
  })

  it('salário de cargo único sem salário na listagem usa o trecho', () => {
    expect(resumoSalario(base({ trechoRemuneracao: 'O salário é de R$ 5.100,00 por mês.' })))
      .toEqual({ tipo: 'trecho', texto: 'O salário é de R$ 5.100,00 por mês.' })
  })

  it('"Vários Cargos" com um cargo listado não usa o salário da listagem como valor', () => {
    expect(resumoSalario(base({ cargo: 'Vários Cargos', cargos: ['Analista de TI (1 vaga)'], salarioMax: 9000, trechoRemuneracao: 'Varia por nível.' })))
      .toEqual({ tipo: 'trecho', texto: 'Varia por nível.' })
  })

  it('taxa: valor, trecho e ausente', () => {
    expect(resumoTaxa(base({ taxaInscricao: 90, trechoTaxa: 'A taxa é de R$ 90,00.' }))).toEqual({ tipo: 'valor', valor: 90 })
    expect(resumoTaxa(base({ trechoTaxa: 'R$ 103,00 para médio e R$ 123,00 para superior.' })))
      .toEqual({ tipo: 'trecho', texto: 'R$ 103,00 para médio e R$ 123,00 para superior.' })
    expect(resumoTaxa(base({}))).toEqual({ tipo: 'ausente' })
  })

  it('inscrições', () => {
    expect(descreverInscricoes('2026-09-28', '2026-11-13')).toBe('28/09/2026 a 13/11/2026')
    expect(descreverInscricoes(null, '2026-11-13')).toBe('até 13/11/2026')
    expect(descreverInscricoes('2026-09-28', null)).toBe('a partir de 28/09/2026')
    expect(descreverInscricoes(null, null)).toBe('não informadas')
  })

  it('cargos do painel', () => {
    expect(resumoCargos(base({ cargos: ['Técnico (9 vagas)', 'Analista - Sistemas (1 vaga)', 'Analista - Redes (1 vaga)'],
      cargosTi: ['Analista - Sistemas (1 vaga)', 'Analista - Redes (1 vaga)'] })))
      .toEqual({ titulo: 'Cargos de TI', itens: ['Analista - Sistemas (1 vaga)', 'Analista - Redes (1 vaga)'], outros: 1 })
    expect(resumoCargos(base({ cargos: ['Analista de TI (1 vaga)'], cargosTi: ['Analista de TI (1 vaga)'] })))
      .toEqual({ titulo: 'Cargo de TI', itens: ['Analista de TI (1 vaga)'], outros: 0 })
    expect(resumoCargos(base({ cargos: ['Médico', 'Contador'] }))).toEqual({ titulo: 'Cargos', itens: ['Médico', 'Contador'], outros: 0 })
    expect(resumoCargos(base({}))).toEqual({ titulo: 'Cargo', itens: ['Analista de Sistemas'], outros: 0 })
    expect(resumoCargos(base({ cargo: null }))).toEqual({ titulo: 'Cargo', itens: ['Cargo não informado'], outros: 0 })
  })

  it('texto de outros cargos', () => {
    expect(textoOutrosCargos(1)).toBe('e mais 1 cargo de outra área')
    expect(textoOutrosCargos(6)).toBe('e mais 6 cargos de outras áreas')
  })
})

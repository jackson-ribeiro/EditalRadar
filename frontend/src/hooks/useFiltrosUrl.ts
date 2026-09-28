import { useCallback, useEffect, useState } from 'react'
import { escreverFiltros, lerFiltros, limparFiltros, type Filtros, type MudancaFiltros } from '../filtros'

export function useFiltrosUrl() {
  const [filtros, setFiltros] = useState<Filtros>(() => lerFiltros(window.location.search))

  useEffect(() => {
    const aoNavegar = () => setFiltros(lerFiltros(window.location.search))
    window.addEventListener('popstate', aoNavegar)
    return () => window.removeEventListener('popstate', aoNavegar)
  }, [])

  const aplicar = useCallback((novos: Filtros, substituir: boolean) => {
    const url = `${window.location.pathname}${escreverFiltros(novos)}${window.location.hash}`
    if (substituir) {
      window.history.replaceState(null, '', url)
    } else {
      window.history.pushState(null, '', url)
    }
    setFiltros(novos)
  }, [])

  const alterar = useCallback(
    (mudancas: MudancaFiltros, opcoes: { substituir?: boolean } = {}) =>
      aplicar({ ...filtros, ...mudancas, page: 0 }, opcoes.substituir ?? false),
    [aplicar, filtros],
  )

  const irParaPagina = useCallback(
    (page: number, opcoes: { substituir?: boolean } = {}) => aplicar({ ...filtros, page }, opcoes.substituir ?? false),
    [aplicar, filtros],
  )

  const limpar = useCallback(() => aplicar(limparFiltros(filtros), false), [aplicar, filtros])

  return { filtros, alterar, irParaPagina, limpar }
}

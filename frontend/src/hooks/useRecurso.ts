import { useCallback, useEffect, useState } from 'react'
import { ErroApi, getJson } from '../api/cliente'

export interface EstadoRecurso<T> {
  dados: T | null
  carregando: boolean
  erro: ErroApi | null
  recarregar: () => void
}

interface Estado<T> {
  dados: T | null
  carregando: boolean
  erro: ErroApi | null
}

export function useRecurso<T>(url: string, versao = 0): EstadoRecurso<T> {
  const [estado, setEstado] = useState<Estado<T>>({ dados: null, carregando: true, erro: null })
  const [tentativa, setTentativa] = useState(0)

  useEffect(() => {
    let ativo = true
    setEstado((atual) => ({ ...atual, carregando: true, erro: null }))
    getJson<T>(url)
      .then((dados) => {
        if (ativo) setEstado({ dados, carregando: false, erro: null })
      })
      .catch((erro: unknown) => {
        if (!ativo) return
        const erroApi = erro instanceof ErroApi ? erro : new ErroApi(String(erro), null)
        setEstado((atual) => ({ dados: atual.dados, carregando: false, erro: erroApi }))
      })
    return () => {
      ativo = false
    }
  }, [url, versao, tentativa])

  const recarregar = useCallback(() => setTentativa((valor) => valor + 1), [])

  return { ...estado, recarregar }
}

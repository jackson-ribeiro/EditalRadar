import { useCallback, useEffect, useRef, useState } from 'react'
import { ErroApi, getJson, postJson } from '../api/cliente'
import type { Coleta } from '../api/tipos'

export const URL_ULTIMA_COLETA = '/api/admin/coletas/ultima'
export const URL_SYNC = '/api/admin/sync'

export interface EstadoSincronizacao {
  ultima: Coleta | null
  carregandoUltima: boolean
  coletando: boolean
  erro: string | null
  atualizar: () => void
}

function mensagem(erro: unknown): string {
  return erro instanceof Error ? erro.message : String(erro)
}

export function useSincronizacao(aoTerminar: () => void, intervaloMs = 3000): EstadoSincronizacao {
  const [ultima, setUltima] = useState<Coleta | null>(null)
  const [carregandoUltima, setCarregandoUltima] = useState(true)
  const [coletando, setColetando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const aoTerminarRef = useRef(aoTerminar)

  useEffect(() => {
    aoTerminarRef.current = aoTerminar
  })

  useEffect(() => {
    let ativo = true
    getJson<Coleta>(URL_ULTIMA_COLETA)
      .then((coleta) => {
        if (!ativo) return
        setUltima(coleta)
        if (coleta?.status === 'EM_ANDAMENTO') setColetando(true)
      })
      .catch((falha: unknown) => {
        if (ativo) setErro(mensagem(falha))
      })
      .finally(() => {
        if (ativo) setCarregandoUltima(false)
      })
    return () => {
      ativo = false
    }
  }, [])

  useEffect(() => {
    if (!coletando) return
    let ativo = true
    const consultar = () => {
      getJson<Coleta>(URL_ULTIMA_COLETA)
        .then((coleta) => {
          if (!ativo) return
          setErro(null)
          setUltima(coleta)
          if (coleta == null || coleta.status !== 'EM_ANDAMENTO') {
            ativo = false
            setColetando(false)
            aoTerminarRef.current()
          }
        })
        .catch((falha: unknown) => {
          if (ativo) setErro(mensagem(falha))
        })
    }
    consultar()
    const intervalo = window.setInterval(consultar, intervaloMs)
    return () => {
      ativo = false
      window.clearInterval(intervalo)
    }
  }, [coletando, intervaloMs])

  const atualizar = useCallback(() => {
    setErro(null)
    postJson(URL_SYNC)
      .then(() => setColetando(true))
      .catch((falha: unknown) => {
        if (falha instanceof ErroApi && falha.status === 409) {
          setColetando(true)
        } else {
          setErro(mensagem(falha))
        }
      })
  }, [])

  return { ultima, carregandoUltima, coletando, erro, atualizar }
}

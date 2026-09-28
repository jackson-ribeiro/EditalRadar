export const MENSAGEM_SEM_SERVIDOR =
  'Não foi possível falar com o servidor. Verifique se o backend está rodando em localhost:8080.'

export class ErroApi extends Error {
  readonly status: number | null

  constructor(mensagem: string, status: number | null) {
    super(mensagem)
    this.name = 'ErroApi'
    this.status = status
  }
}

async function requisitar<T>(url: string, init?: RequestInit): Promise<T | null> {
  let resposta: Response
  try {
    resposta = await fetch(url, { ...init, headers: { Accept: 'application/json', ...init?.headers } })
  } catch {
    throw new ErroApi(MENSAGEM_SEM_SERVIDOR, null)
  }
  if (resposta.status === 204) return null
  const corpo = await lerCorpo(resposta)
  if (!resposta.ok) throw new ErroApi(mensagemDeErro(corpo, resposta.status), resposta.status)
  return corpo as T
}

async function lerCorpo(resposta: Response): Promise<unknown> {
  const texto = await resposta.text()
  if (!texto) return null
  try {
    return JSON.parse(texto)
  } catch {
    return texto
  }
}

function mensagemDeErro(corpo: unknown, status: number): string {
  if (corpo && typeof corpo === 'object' && 'detail' in corpo && typeof corpo.detail === 'string') {
    return corpo.detail
  }
  if (status >= 500 && (corpo == null || corpo === '')) return MENSAGEM_SEM_SERVIDOR
  return `O servidor respondeu com erro (HTTP ${status}).`
}

export function getJson<T>(url: string): Promise<T | null> {
  return requisitar<T>(url)
}

export function postJson<T>(url: string): Promise<T | null> {
  return requisitar<T>(url, { method: 'POST' })
}

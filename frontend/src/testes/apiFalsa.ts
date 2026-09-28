import { vi } from 'vitest'

export function respostaJson(corpo: unknown, status = 200): Response {
  const texto = corpo === undefined ? '' : JSON.stringify(corpo)
  return { status, ok: status >= 200 && status < 300, text: async () => texto } as unknown as Response
}

export function instalarFetch(tratador: (url: string, init?: RequestInit) => Response) {
  const mock = vi.fn(async (url: string | URL | Request, init?: RequestInit) => tratador(String(url), init))
  vi.stubGlobal('fetch', mock)
  return mock
}

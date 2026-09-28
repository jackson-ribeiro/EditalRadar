export function Carregando({ rotulo, linhas = 5 }: { rotulo: string; linhas?: number }) {
  return (
    <div role="status" aria-label={rotulo} className="space-y-3 py-2">
      {Array.from({ length: linhas }, (_, indice) => (
        <div key={indice} className="h-3 rounded bg-mesa" style={{ width: `${90 - (indice % 3) * 10}%` }} />
      ))}
    </div>
  )
}

export function Vazio({
  mensagem,
  acao,
}: {
  mensagem: string
  acao?: { rotulo: string; aoClicar: () => void }
}) {
  return (
    <div className="py-8 text-grafite">
      <p>{mensagem}</p>
      {acao && (
        <button
          type="button"
          onClick={acao.aoClicar}
          className="mt-3 rounded-md border border-caneta px-3 py-1 text-[13px] text-caneta hover:bg-caneta/10"
        >
          {acao.rotulo}
        </button>
      )}
    </div>
  )
}

export function Erro({ mensagem, aoTentarDeNovo }: { mensagem: string; aoTentarDeNovo: () => void }) {
  return (
    <div role="alert" className="py-8 text-grafite">
      <p>{mensagem}</p>
      <button
        type="button"
        onClick={aoTentarDeNovo}
        className="mt-3 rounded-md border border-caneta px-3 py-1 text-[13px] text-caneta hover:bg-caneta/10"
      >
        Tentar de novo
      </button>
    </div>
  )
}

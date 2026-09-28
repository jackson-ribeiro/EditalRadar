const BOTAO =
  'rounded-md px-2 py-1 text-caneta hover:bg-caneta/10 disabled:cursor-not-allowed disabled:text-grafite disabled:opacity-50 disabled:hover:bg-transparent'

export function Paginacao({
  page,
  totalPages,
  aoMudar,
}: {
  page: number
  totalPages: number
  aoMudar: (page: number) => void
}) {
  if (totalPages <= 1) return null
  return (
    <nav aria-label="Paginação" className="flex items-center justify-end gap-4 pt-4 text-[13px]">
      <button type="button" className={BOTAO} disabled={page <= 0} onClick={() => aoMudar(page - 1)}>
        ‹ Anterior
      </button>
      <span className="text-grafite">
        Página {page + 1} de {totalPages}
      </span>
      <button type="button" className={BOTAO} disabled={page >= totalPages - 1} onClick={() => aoMudar(page + 1)}>
        Próxima ›
      </button>
    </nav>
  )
}

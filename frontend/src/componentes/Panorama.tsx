import { useState, type ReactNode } from 'react'

export function Panorama({ children }: { children: ReactNode }) {
  const [abertoNoCelular, setAbertoNoCelular] = useState(false)
  return (
    <section aria-label="Distribuição dos concursos" className="mt-4">
      <button
        type="button"
        className="text-[13px] text-caneta md:hidden"
        aria-expanded={abertoNoCelular}
        aria-controls="panorama-graficos"
        onClick={() => setAbertoNoCelular((valor) => !valor)}
      >
        {abertoNoCelular ? 'Esconder distribuição' : 'Ver distribuição'}
      </button>
      <div
        id="panorama-graficos"
        className={`${abertoNoCelular ? 'grid' : 'hidden'} mt-3 gap-6 md:mt-0 md:grid md:grid-cols-[1.4fr_1fr] md:gap-10`}
      >
        {children}
      </div>
    </section>
  )
}

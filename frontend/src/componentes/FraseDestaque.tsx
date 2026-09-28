import type { Resumo } from '../api/tipos'
import { fraseDestaque } from '../formatos'

export function FraseDestaque({ resumo }: { resumo: Resumo | null }) {
  if (!resumo) return null
  const { destaque, resto } = fraseDestaque(resumo.encerrandoEm7Dias)
  return (
    <p className="mt-6 mb-4 max-w-[26ch] font-titulo text-[22px] leading-tight font-medium md:text-[28px]">
      {destaque && <span className="font-bold text-vencendo">{destaque}</span>}
      {resto}
    </p>
  )
}

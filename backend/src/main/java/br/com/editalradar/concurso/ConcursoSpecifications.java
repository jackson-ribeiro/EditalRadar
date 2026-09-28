package br.com.editalradar.concurso;

import br.com.editalradar.comum.Textos;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ConcursoSpecifications {

    private ConcursoSpecifications() {
    }

    public static Specification<Concurso> comFiltro(ConcursoFiltro filtro) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.status() != null) {
                condicoes.add(cb.equal(raiz.get("status"), filtro.status()));
            }
            if (temTexto(filtro.uf())) {
                if (filtro.uf().strip().equalsIgnoreCase("NACIONAL")) {
                    condicoes.add(cb.isTrue(raiz.<Boolean>get("nacional")));
                } else {
                    condicoes.add(cb.equal(raiz.get("uf"), filtro.uf().strip().toUpperCase(Locale.ROOT)));
                    condicoes.add(cb.isFalse(raiz.<Boolean>get("nacional")));
                }
            }
            if (temTexto(filtro.banca())) {
                condicoes.add(cb.equal(cb.lower(raiz.<String>get("banca")), filtro.banca().strip().toLowerCase(Locale.ROOT)));
            }
            if (filtro.salarioMin() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(raiz.<BigDecimal>get("salarioMax"), filtro.salarioMin()));
            }
            if (temTexto(filtro.escolaridade())) {
                condicoes.add(contem(cb, raiz.<String>get("escolaridade"), filtro.escolaridade()));
            }
            if (temTexto(filtro.q())) {
                condicoes.add(cb.or(
                        contem(cb, raiz.<String>get("orgao"), filtro.q()),
                        contem(cb, raiz.<String>get("titulo"), filtro.q()),
                        contem(cb, raiz.<String>get("cargo"), filtro.q()),
                        contem(cb, raiz.<String>get("cargos"), filtro.q())));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }

    public static Specification<Concurso> ordenadoPor(OrdenacaoConcurso ordenacao) {
        return (raiz, consulta, cb) -> {
            if (!ehContagem(consulta)) {
                Path<Object> campo = raiz.get(ordenacao.campo());
                Order nulosPorUltimo = cb.asc(cb.<Integer>selectCase().when(cb.isNull(campo), 1).otherwise(0));
                Order principal = ordenacao.crescente() ? cb.asc(campo) : cb.desc(campo);
                consulta.orderBy(nulosPorUltimo, principal, cb.asc(raiz.get("id")));
            }
            return null;
        };
    }

    public static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Predicate contem(CriteriaBuilder cb, Expression<String> coluna, String termo) {
        String padrao = "%" + escaparLike(Textos.normalizar(termo)) + "%";
        return cb.like(cb.lower(cb.function("unaccent", String.class, coluna)), padrao, '\\');
    }

    private static boolean ehContagem(CriteriaQuery<?> consulta) {
        return Long.class.equals(consulta.getResultType()) || long.class.equals(consulta.getResultType());
    }

    private static boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}

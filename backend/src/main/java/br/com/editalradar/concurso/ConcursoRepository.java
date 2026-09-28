package br.com.editalradar.concurso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConcursoRepository extends JpaRepository<Concurso, Long>, JpaSpecificationExecutor<Concurso> {

    Optional<Concurso> findByUrlOrigem(String urlOrigem);

    List<Concurso> findByUrlOrigemIn(Collection<String> urlsOrigem);

    List<Concurso> findByStatus(StatusConcurso status);

    List<Concurso> findByStatusAndDetalheColetadoEmIsNull(StatusConcurso status);

    long countByStatus(StatusConcurso status);

    long countByStatusAndFimInscricaoBetween(StatusConcurso status, LocalDate inicio, LocalDate fim);

    long countByStatusInAndPrimeiraVezVistoEmGreaterThanEqual(Collection<StatusConcurso> status, Instant desde);

    @Query("select c.uf, c.nacional, count(c) from Concurso c where c.status = :status group by c.uf, c.nacional")
    List<Object[]> contarPorUf(@Param("status") StatusConcurso status);

    @Query("select c.banca, count(c) from Concurso c where c.status = :status group by c.banca")
    List<Object[]> contarPorBanca(@Param("status") StatusConcurso status);

    @Query("select c.salarioMax from Concurso c where c.status = :status")
    List<BigDecimal> salariosMaximos(@Param("status") StatusConcurso status);
}

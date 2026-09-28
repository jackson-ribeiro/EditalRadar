package br.com.editalradar.coleta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColetaRepository extends JpaRepository<Coleta, Long> {

    Optional<Coleta> findFirstByOrderByIniciadaEmDesc();

    Optional<Coleta> findFirstByStatusOrderByIniciadaEmDesc(StatusColeta status);

    List<Coleta> findByStatus(StatusColeta status);
}

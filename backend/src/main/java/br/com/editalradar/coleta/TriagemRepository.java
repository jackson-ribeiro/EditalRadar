package br.com.editalradar.coleta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TriagemRepository extends JpaRepository<Triagem, String> {

    List<Triagem> findByUrlOrigemIn(Collection<String> urlsOrigem);

    List<Triagem> findBySituacao(SituacaoTriagem situacao);

    long countBySituacao(SituacaoTriagem situacao);

    @Modifying
    @Query("delete from Triagem t where t.urlOrigem not in :urls")
    int removerForaDe(@Param("urls") Collection<String> urls);
}

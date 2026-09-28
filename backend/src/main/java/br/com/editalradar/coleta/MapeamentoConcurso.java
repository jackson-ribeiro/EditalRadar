package br.com.editalradar.coleta;

import br.com.editalradar.concurso.Concurso;
import br.com.editalradar.concurso.OrigemTi;
import br.com.editalradar.concurso.StatusConcurso;
import br.com.editalradar.scraper.ConcursoListado;
import br.com.editalradar.scraper.DatasTexto.SituacaoPrazo;
import br.com.editalradar.scraper.DetalheConcurso;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;

final class MapeamentoConcurso {

    private MapeamentoConcurso() {
    }

    static Concurso novo(ConcursoListado item, OrigemTi origem, Instant agora, LocalDate hoje) {
        Concurso concurso = new Concurso();
        concurso.setUrlOrigem(item.urlOrigem());
        concurso.setOrigemTi(origem);
        concurso.setPrimeiraVezVistoEm(agora);
        concurso.setUltimaVezVistoEm(agora);
        concurso.setAtualizadoEm(agora);
        aplicarListagem(concurso, item, hoje);
        return concurso;
    }

    static boolean aplicarListagem(Concurso concurso, ConcursoListado item, LocalDate hoje) {
        boolean mudou = false;
        mudou |= mudar(concurso.getOrgao(), item.orgao(), concurso::setOrgao);
        mudou |= mudar(concurso.getTitulo(), item.titulo(), concurso::setTitulo);
        mudou |= mudar(concurso.getUf(), item.uf(), concurso::setUf);
        mudou |= mudar(concurso.isNacional(), item.nacional(), concurso::setNacional);
        mudou |= mudar(concurso.getCargo(), item.cargo(), concurso::setCargo);
        mudou |= mudar(concurso.getEscolaridade(), item.escolaridade(), concurso::setEscolaridade);
        mudou |= mudar(concurso.getVagas(), item.vagas(), concurso::setVagas);
        mudou |= mudar(concurso.isCadastroReserva(), item.cadastroReserva(), concurso::setCadastroReserva);
        mudou |= mudarSeInformado(concurso.getSalarioMin(), item.salarioMin(), concurso::setSalarioMin);
        mudou |= mudarSeInformado(concurso.getSalarioMax(), item.salarioMax(), concurso::setSalarioMax);
        mudou |= mudarSeInformado(concurso.getInicioInscricao(), item.inicioInscricao(), concurso::setInicioInscricao);
        mudou |= mudarSeInformado(concurso.getFimInscricao(), item.fimInscricao(), concurso::setFimInscricao);
        mudou |= mudar(concurso.getStatus(), statusPara(item.situacaoPrazo(), concurso.getFimInscricao(), hoje),
                concurso::setStatus);
        return mudou;
    }

    static boolean aplicarDetalhe(Concurso concurso, DetalheConcurso detalhe) {
        boolean mudou = false;
        if (!detalhe.cargos().isEmpty()) {
            mudou |= mudar(concurso.getCargos(), String.join("\n", detalhe.cargos()), concurso::setCargos);
        }
        mudou |= mudarSeInformado(concurso.getBanca(), detalhe.banca(), concurso::setBanca);
        mudou |= mudarSeInformado(concurso.getDataProva(), detalhe.dataProva(), concurso::setDataProva);
        mudou |= mudarSeInformado(concurso.getTrechoRemuneracao(), detalhe.trechoRemuneracao(), concurso::setTrechoRemuneracao);
        if (detalhe.trechoTaxa() != null) {
            // A taxa em valor sempre acompanha o trecho de onde saiu, para não contradizê-lo.
            mudou |= mudar(concurso.getTrechoTaxa(), detalhe.trechoTaxa(), concurso::setTrechoTaxa);
            mudou |= mudar(concurso.getTaxaInscricao(), detalhe.taxaInscricao(), concurso::setTaxaInscricao);
        }
        if (concurso.getInicioInscricao() == null) {
            mudou |= mudarSeInformado(null, detalhe.inicioInscricao(), concurso::setInicioInscricao);
        }
        if (concurso.getSalarioMin() == null) {
            mudou |= mudarSeInformado(null, detalhe.salarioMin(), concurso::setSalarioMin);
        }
        if (concurso.getSalarioMax() == null) {
            mudou |= mudarSeInformado(null, detalhe.salarioMax(), concurso::setSalarioMax);
        }
        return mudou;
    }

    static StatusConcurso statusPara(SituacaoPrazo situacao, LocalDate fim, LocalDate hoje) {
        if (situacao == SituacaoPrazo.SUSPENSO || situacao == SituacaoPrazo.CANCELADO) {
            return StatusConcurso.ENCERRADO;
        }
        if (fim != null && fim.isBefore(hoje)) {
            return StatusConcurso.ENCERRADO;
        }
        return StatusConcurso.ABERTO;
    }

    private static <T> boolean mudar(T atual, T novo, Consumer<T> setter) {
        if (iguais(atual, novo)) {
            return false;
        }
        setter.accept(novo);
        return true;
    }

    private static <T> boolean mudarSeInformado(T atual, T novo, Consumer<T> setter) {
        return novo != null && mudar(atual, novo, setter);
    }

    private static boolean iguais(Object atual, Object novo) {
        if (atual instanceof BigDecimal a && novo instanceof BigDecimal b) {
            return a.compareTo(b) == 0;
        }
        return Objects.equals(atual, novo);
    }
}

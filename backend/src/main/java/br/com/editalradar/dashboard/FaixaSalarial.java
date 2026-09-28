package br.com.editalradar.dashboard;

import java.math.BigDecimal;

record FaixaSalarial(String rotulo, BigDecimal minimoInclusivo, BigDecimal maximoExclusivo) {

    boolean contem(BigDecimal valor) {
        if (valor == null) {
            return false;
        }
        boolean acimaDoMinimo = minimoInclusivo == null || valor.compareTo(minimoInclusivo) >= 0;
        boolean abaixoDoMaximo = maximoExclusivo == null || valor.compareTo(maximoExclusivo) < 0;
        return acimaDoMinimo && abaixoDoMaximo;
    }
}

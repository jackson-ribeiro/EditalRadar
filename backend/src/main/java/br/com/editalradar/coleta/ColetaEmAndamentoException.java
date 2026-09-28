package br.com.editalradar.coleta;

public class ColetaEmAndamentoException extends RuntimeException {

    public ColetaEmAndamentoException() {
        super("Já existe uma coleta em andamento. Aguarde ela terminar.");
    }
}

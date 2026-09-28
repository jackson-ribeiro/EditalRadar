package br.com.editalradar.web;

public class ParametroInvalidoException extends RuntimeException {

    public ParametroInvalidoException(String mensagem) {
        super(mensagem);
    }
}

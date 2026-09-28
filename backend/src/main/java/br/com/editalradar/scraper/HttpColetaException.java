package br.com.editalradar.scraper;

public class HttpColetaException extends RuntimeException {

    public HttpColetaException(String mensagem) {
        super(mensagem);
    }

    public HttpColetaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

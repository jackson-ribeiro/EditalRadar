package br.com.editalradar.scraper;

public class McpException extends RuntimeException {

    public McpException(String mensagem) {
        super(mensagem);
    }

    public McpException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

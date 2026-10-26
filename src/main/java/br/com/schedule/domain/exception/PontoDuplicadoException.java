package br.com.schedule.domain.exception;

public class PontoDuplicadoException extends DomainException {

    public PontoDuplicadoException(String codigo, String ronda) {
        super(String.format("O código de ponto '%s' está duplicado na ronda '%s'.", codigo, ronda));
    }
}

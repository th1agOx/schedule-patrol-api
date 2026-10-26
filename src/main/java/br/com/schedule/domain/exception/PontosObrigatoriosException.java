package br.com.schedule.domain.exception;

public class PontosObrigatoriosException extends DomainException {

    public PontosObrigatoriosException() {
        super("A ronda deve possuir ao menos um ponto.");
    }
}

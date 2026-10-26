package br.com.schedule.domain.exception;

public class RondaInativaException extends DomainException {

    public RondaInativaException(String idRonda) {
        super("A ronda '%s' está inativa e não pode ser agendada.".formatted(idRonda));
    }
}

package br.com.schedule.domain.exception;

import java.time.Instant;

public class InicioNoPassadoException extends DomainException {

    public InicioNoPassadoException(Instant inicio, Instant agora) {
        super("Não é possível agendar um evento para o passado.");
    }
}

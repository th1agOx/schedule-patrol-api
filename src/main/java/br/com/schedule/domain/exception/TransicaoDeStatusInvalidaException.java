package br.com.schedule.domain.exception;

import br.com.schedule.domain.model.StatusAgendamento;

public class TransicaoDeStatusInvalidaException extends DomainException {

    public TransicaoDeStatusInvalidaException(StatusAgendamento atual, StatusAgendamento destino) {
        super("Transição de status inválida: %s -> %s.".formatted(atual, destino));
    }
}

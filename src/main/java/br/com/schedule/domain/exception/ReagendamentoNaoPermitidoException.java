package br.com.schedule.domain.exception;

import br.com.schedule.domain.model.StatusAgendamento;

public class ReagendamentoNaoPermitidoException extends DomainException {

    public ReagendamentoNaoPermitidoException(StatusAgendamento statusAtual) {
        super("Somente agendamentos com status AGENDADA podem ser reagendados. Status atual: %s.".formatted(statusAtual));
    }
}

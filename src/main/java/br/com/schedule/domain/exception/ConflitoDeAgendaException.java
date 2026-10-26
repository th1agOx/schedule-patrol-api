package br.com.schedule.domain.exception;

import br.com.schedule.domain.model.Periodo;

public class ConflitoDeAgendaException extends DomainException {

    public ConflitoDeAgendaException(String responsavel, Periodo periodo) {
        super("Já existe agendamento ativo para o responsável '%s' no período de %s a %s."
                .formatted(responsavel, periodo.inicio(), periodo.fim()));
    }
}

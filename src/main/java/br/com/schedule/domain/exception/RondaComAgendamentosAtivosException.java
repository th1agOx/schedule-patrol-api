package br.com.schedule.domain.exception;

public class RondaComAgendamentosAtivosException extends DomainException {

    public RondaComAgendamentosAtivosException(String idRonda) {
        super("A ronda '%s' possui agendamentos ativos e não pode ser excluída.".formatted(idRonda));
    }
}

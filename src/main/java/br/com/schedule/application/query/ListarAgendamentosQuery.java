package br.com.schedule.application.query;

import br.com.schedule.domain.model.StatusAgendamento;

public record ListarAgendamentosQuery(StatusAgendamento status, String responsavel, String idRonda) {
}

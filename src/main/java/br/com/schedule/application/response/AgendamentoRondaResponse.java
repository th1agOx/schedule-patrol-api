package br.com.schedule.application.response;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;

public record AgendamentoRondaResponse(String id, String idRonda, String responsavel,
        PeriodoResponse periodoPrevisto, StatusAgendamento status, String motivoCancelamento) {

    public static AgendamentoRondaResponse from(AgendamentoRonda agendamento) {
        return new AgendamentoRondaResponse(
                agendamento.getId(),
                agendamento.getIdRonda(),
                agendamento.getResponsavel(),
                PeriodoResponse.from(agendamento.getPeriodoPrevisto()),
                agendamento.getStatus(),
                agendamento.getMotivoCancelamento().orElse(null));
    }
}

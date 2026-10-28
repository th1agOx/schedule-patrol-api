package br.com.schedule.application.mapper;

import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.application.dto.PeriodoDTO;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;

public final class AgendamentoDTOMapper {

    private AgendamentoDTOMapper() {
    }

    public static Periodo paraPeriodo(PeriodoDTO periodo) {
        return periodo == null ? null : new Periodo(periodo.inicio(), periodo.fim());
    }

    public static AgendamentoResponseDTO paraResponse(AgendamentoRonda agendamento) {
        Periodo periodo = agendamento.getPeriodoPrevisto();

        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getIdRonda(),
                agendamento.getResponsavel(),
                new PeriodoDTO(periodo.inicio(), periodo.fim()),
                agendamento.getStatus().name(),
                agendamento.getMotivoCancelamento().orElse(null));
    }
}

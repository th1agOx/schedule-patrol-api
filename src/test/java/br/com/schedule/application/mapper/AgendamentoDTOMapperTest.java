package br.com.schedule.application.mapper;

import br.com.schedule.application.dto.PeriodoDTO;
import br.com.schedule.application.dto.response.AgendamentoResponseDTO;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import org.junit.jupiter.api.Test;

import static br.com.schedule.application.ApplicationTestFixtures.FIM;
import static br.com.schedule.application.ApplicationTestFixtures.INICIO;
import static br.com.schedule.application.ApplicationTestFixtures.PERIODO;
import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static org.assertj.core.api.Assertions.assertThat;

class AgendamentoDTOMapperTest {

    @Test
    void shouldMapPeriodoDtoToDomain() {
        assertThat(AgendamentoDTOMapper.paraPeriodo(new PeriodoDTO(INICIO, FIM))).isEqualTo(PERIODO);
    }

    @Test
    void shouldMapNullPeriodoDtoToNull() {
        assertThat(AgendamentoDTOMapper.paraPeriodo(null)).isNull();
    }

    @Test
    void shouldMapAgendamentoToResponse() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", CANCELADA);

        AgendamentoResponseDTO response = AgendamentoDTOMapper.paraResponse(agendamento);

        assertThat(response.id()).isEqualTo("agendamento-1");
        assertThat(response.idRonda()).isEqualTo("ronda-1");
        assertThat(response.responsavel()).isEqualTo("Maria");
        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoDTO(INICIO, FIM));
        assertThat(response.status()).isEqualTo("CANCELADA");
        assertThat(response.motivoCancelamento()).isEqualTo("Sem equipe");
    }

    @Test
    void shouldMapActiveAgendamentoWithoutCancellationReason() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", AGENDADA);

        AgendamentoResponseDTO response = AgendamentoDTOMapper.paraResponse(agendamento);

        assertThat(response.status()).isEqualTo("AGENDADA");
        assertThat(response.motivoCancelamento()).isNull();
    }
}

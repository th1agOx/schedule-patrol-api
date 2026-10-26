package br.com.schedule.infrastructure.persistence.mapper;

import br.com.schedule.domain.exception.AgendamentoInvalidoException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.document.PeriodoDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgendamentoRondaMapperTest {

    private static final Instant INICIO = Instant.parse("2026-11-05T10:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-05T12:00:00Z");
    private static final Periodo PERIODO = new Periodo(INICIO, FIM);

    @Test
    void shouldMapAgendadaToDocument() {
        AgendamentoRonda agendamento = agendamento(AGENDADA, null);

        AgendamentoRondaDocument document = AgendamentoRondaMapper.toDocument(agendamento);

        assertThat(document.getId()).isEqualTo("agendamento-1");
        assertThat(document.getIdRonda()).isEqualTo("ronda-1");
        assertThat(document.getResponsavel()).isEqualTo("Maria");
        assertThat(document.getPeriodoPrevisto().getInicio()).isEqualTo(INICIO);
        assertThat(document.getPeriodoPrevisto().getFim()).isEqualTo(FIM);
        assertThat(document.getStatus()).isEqualTo("AGENDADA");
        assertThat(document.getMotivoCancelamento()).isNull();
    }

    @Test
    void shouldMapCancelledToDocument() {
        AgendamentoRonda agendamento = agendamento(CANCELADA, "Sem equipe");

        AgendamentoRondaDocument document = AgendamentoRondaMapper.toDocument(agendamento);

        assertThat(document.getStatus()).isEqualTo("CANCELADA");
        assertThat(document.getMotivoCancelamento()).isEqualTo("Sem equipe");
    }

    @Test
    void shouldMapDocumentToDomain() {
        AgendamentoRondaDocument document = new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                new PeriodoDocument(INICIO, FIM), "EM_ANDAMENTO", null);

        AgendamentoRonda agendamento = AgendamentoRondaMapper.toDomain(document);

        assertThat(agendamento.getId()).isEqualTo("agendamento-1");
        assertThat(agendamento.getIdRonda()).isEqualTo("ronda-1");
        assertThat(agendamento.getResponsavel()).isEqualTo("Maria");
        assertThat(agendamento.getPeriodoPrevisto()).isEqualTo(PERIODO);
        assertThat(agendamento.getStatus()).isEqualTo(EM_ANDAMENTO);
        assertThat(agendamento.getMotivoCancelamento()).isEmpty();
    }

    @Test
    void shouldRoundTripAllStatuses() {
        for (StatusAgendamento status : StatusAgendamento.values()) {
            String motivo = status == CANCELADA ? "Sem equipe" : null;
            AgendamentoRonda original = agendamento(status, motivo);

            AgendamentoRondaDocument document = AgendamentoRondaMapper.toDocument(original);
            AgendamentoRonda roundTrip = AgendamentoRondaMapper.toDomain(document);

            assertThat(document.getStatus()).as("status persistido de %s", status).isEqualTo(status.name());
            assertThat(roundTrip.getStatus()).isEqualTo(status);
            assertThat(roundTrip.getPeriodoPrevisto()).isEqualTo(PERIODO);
            assertThat(roundTrip.getMotivoCancelamento()).isEqualTo(original.getMotivoCancelamento());
        }
    }

    @Test
    void shouldRejectUnknownStatus() {
        AgendamentoRondaDocument document = new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                new PeriodoDocument(INICIO, FIM), "PAUSADA", null);

        assertThatThrownBy(() -> AgendamentoRondaMapper.toDomain(document))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAUSADA")
                .hasMessageContaining("agendamento-1");
    }

    @Test
    void shouldRejectMissingStatus() {
        AgendamentoRondaDocument document = new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                new PeriodoDocument(INICIO, FIM), null, null);

        assertThatThrownBy(() -> AgendamentoRondaMapper.toDomain(document))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não foi informado")
                .hasMessageContaining("agendamento-1");
    }

    @Test
    void shouldRejectDocumentWithoutPeriod() {
        AgendamentoRondaDocument document = new AgendamentoRondaDocument("agendamento-1", "ronda-1", "Maria",
                null, "AGENDADA", null);

        assertThatThrownBy(() -> AgendamentoRondaMapper.toDomain(document))
                .isExactlyInstanceOf(AgendamentoInvalidoException.class)
                .hasMessageContaining("período");
    }

    private static AgendamentoRonda agendamento(StatusAgendamento status, String motivo) {
        return AgendamentoRonda.reconstituir("agendamento-1", "ronda-1", "Maria", PERIODO, status, motivo);
    }
}

package br.com.schedule.infrastructure.persistence.mapper;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.infrastructure.persistence.document.AgendamentoRondaDocument;
import br.com.schedule.infrastructure.persistence.document.PeriodoDocument;

import java.util.Objects;


public final class AgendamentoRondaMapper {

    private AgendamentoRondaMapper() {
    }

    public static AgendamentoRondaDocument toDocument(AgendamentoRonda agendamento) {
        Objects.requireNonNull(agendamento, "O agendamento é obrigatório para o mapeamento.");

        Periodo periodo = agendamento.getPeriodoPrevisto();
        PeriodoDocument periodoDocument = new PeriodoDocument(periodo.inicio(), periodo.fim());

        return new AgendamentoRondaDocument(agendamento.getId(), agendamento.getIdRonda(),
                agendamento.getResponsavel(), periodoDocument, agendamento.getStatus().name(),
                agendamento.getMotivoCancelamento().orElse(null));
    }

    public static AgendamentoRonda toDomain(AgendamentoRondaDocument document) {
        Objects.requireNonNull(document, "O documento do agendamento é obrigatório para o mapeamento.");

        PeriodoDocument periodoDocument = document.getPeriodoPrevisto();
        Periodo periodo = periodoDocument == null
                ? null
                : new Periodo(periodoDocument.getInicio(), periodoDocument.getFim());

        return AgendamentoRonda.reconstituir(document.getId(), document.getIdRonda(), document.getResponsavel(),
                periodo, toStatus(document.getStatus(), document.getId()), document.getMotivoCancelamento());
    }

    private static StatusAgendamento toStatus(String status, String idAgendamento) {
        if (status == null || status.isBlank()) {
            throw new IllegalStateException(
                    "O status do agendamento '%s' não foi informado.".formatted(idAgendamento));
        }
        try {
            return StatusAgendamento.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "O status '%s' do agendamento '%s' é inválido.".formatted(status, idAgendamento), exception);
        }
    }
}

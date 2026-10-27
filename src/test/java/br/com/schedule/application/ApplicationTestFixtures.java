package br.com.schedule.application;

import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.model.StatusAgendamento;

import java.time.Instant;
import java.util.List;

public final class ApplicationTestFixtures {

    public static final Instant AGORA = Instant.parse("2026-11-01T10:00:00Z");
    public static final Instant INICIO = Instant.parse("2026-11-05T10:00:00Z");
    public static final Instant FIM = Instant.parse("2026-11-05T12:00:00Z");
    public static final Periodo PERIODO = new Periodo(INICIO, FIM);

    private ApplicationTestFixtures() {
    }

    public static Ronda ronda(String id) {
        return ronda(id, true);
    }

    public static Ronda ronda(String id, boolean ativa) {
        return Ronda.reconstituir(id, "Ronda Norte", "Inspeção diária", ativa,
                List.of(new Ponto("P-01", "Portão principal")));
    }

    public static AgendamentoRonda agendamento(String id, StatusAgendamento status) {
        return AgendamentoRonda.reconstituir(id, "ronda-1", "Maria", PERIODO, status,
                status == StatusAgendamento.CANCELADA ? "Sem equipe" : null);
    }
}

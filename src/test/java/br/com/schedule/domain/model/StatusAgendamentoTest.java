package br.com.schedule.domain.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static br.com.schedule.domain.model.StatusAgendamento.AGENDADA;
import static br.com.schedule.domain.model.StatusAgendamento.CANCELADA;
import static br.com.schedule.domain.model.StatusAgendamento.CONCLUIDA;
import static br.com.schedule.domain.model.StatusAgendamento.EM_ANDAMENTO;
import static org.assertj.core.api.Assertions.assertThat;

class StatusAgendamentoTest {

    @Test
    void shouldAllowOnlyTheExpectedTransitions() {
        Map<StatusAgendamento, Set<StatusAgendamento>> transicoesEsperadas = Map.of(
                AGENDADA, EnumSet.of(EM_ANDAMENTO, CANCELADA),
                EM_ANDAMENTO, EnumSet.of(CONCLUIDA),
                CONCLUIDA, EnumSet.noneOf(StatusAgendamento.class),
                CANCELADA, EnumSet.noneOf(StatusAgendamento.class));

        for (StatusAgendamento atual : StatusAgendamento.values()) {
            for (StatusAgendamento destino : StatusAgendamento.values()) {
                assertThat(atual.podeTransicionarPara(destino))
                        .as("%s -> %s", atual, destino)
                        .isEqualTo(transicoesEsperadas.get(atual).contains(destino));
            }
        }
    }

    @Test
    void shouldNotAllowTransitionToNull() {
        for (StatusAgendamento status : StatusAgendamento.values()) {
            assertThat(status.podeTransicionarPara(null)).isFalse();
        }
    }

    @Test
    void shouldIdentifyActiveStatuses() {
        assertThat(AGENDADA.estaAtivo()).isTrue();
        assertThat(EM_ANDAMENTO.estaAtivo()).isTrue();
        assertThat(CONCLUIDA.estaAtivo()).isFalse();
        assertThat(CANCELADA.estaAtivo()).isFalse();
    }
}

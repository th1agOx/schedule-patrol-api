package br.com.schedule.domain.service;

import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.Ponto;
import br.com.schedule.domain.model.Ronda;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificadorDeConflitoDeAgendaTest {

    private static final Instant AGORA = Instant.parse("2026-10-26T12:00:00Z");
    private static final Instant INICIO = Instant.parse("2026-11-05T10:00:00Z");
    private static final Periodo PERIODO = new Periodo(INICIO, INICIO.plusSeconds(7_200));

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private VerificadorDeConflitoDeAgenda verificador;

    @BeforeEach
    void setUp() {
        verificador = new VerificadorDeConflitoDeAgenda(agendamentoRepository);
    }

    @Test
    void shouldRejectOverlappingActiveAgendamentoForTheSameResponsavel() {
        AgendamentoRonda existente = agendamento("Maria",
                new Periodo(INICIO.plusSeconds(1_800), INICIO.plusSeconds(5_400)));
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of(existente));

        assertThatThrownBy(() -> verificador.verificar("Maria", PERIODO))
                .isExactlyInstanceOf(ConflitoDeAgendaException.class)
                .hasMessageContaining("Maria");
    }

    @Test
    void shouldAllowWhenPeriodsOnlyTouch() {
        AgendamentoRonda existente = agendamento("Maria", new Periodo(INICIO.minusSeconds(7_200), INICIO));
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of(existente));

        assertThatCode(() -> verificador.verificar("Maria", PERIODO)).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowWhenPeriodsAreDisjoint() {
        AgendamentoRonda existente = agendamento("Maria",
                new Periodo(INICIO.plusSeconds(7_200), INICIO.plusSeconds(10_800)));
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of(existente));

        assertThatCode(() -> verificador.verificar("Maria", PERIODO)).doesNotThrowAnyException();
    }

    @Test
    void shouldAllowWhenThereIsNoActiveAgendamento() {
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of());

        assertThatCode(() -> verificador.verificar("Maria", PERIODO)).doesNotThrowAnyException();
    }

    @Test
    void shouldIgnoreTheAgendamentoBeingRescheduled() {
        AgendamentoRonda existente = agendamento("Maria", PERIODO);
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of(existente));

        assertThatCode(() -> verificador.verificar("Maria", PERIODO, existente.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectAnotherAgendamentoEvenWhenAnIgnoredIdIsInformed() {
        AgendamentoRonda existente = agendamento("Maria", PERIODO);
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of(existente));

        assertThatThrownBy(() -> verificador.verificar("Maria", PERIODO, "outro-id"))
                .isExactlyInstanceOf(ConflitoDeAgendaException.class);
    }

    @Test
    void shouldQueryActiveAgendamentosByResponsavel() {
        when(agendamentoRepository.buscarAtivosPorResponsavel("Maria")).thenReturn(List.of());

        verificador.verificar("Maria", PERIODO);

        verify(agendamentoRepository).buscarAtivosPorResponsavel("Maria");
    }

    private static AgendamentoRonda agendamento(String responsavel, Periodo periodo) {
        Ronda ronda = Ronda.criar("Ronda Norte", null, List.of(new Ponto("P-01", "Portão principal")));
        return AgendamentoRonda.agendar(ronda, responsavel, periodo, AGORA);
    }
}

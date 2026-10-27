package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.PeriodoCommand;
import br.com.schedule.application.command.ReagendarRondaCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.application.response.PeriodoResponse;
import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.exception.InicioNoPassadoException;
import br.com.schedule.domain.exception.ReagendamentoNaoPermitidoException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.Periodo;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.AGORA;
import static br.com.schedule.application.ApplicationTestFixtures.FIM;
import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReagendarRondaUseCaseTest {

    private static final Instant NOVO_INICIO = Instant.parse("2026-11-06T10:00:00Z");
    private static final Instant NOVO_FIM = Instant.parse("2026-11-06T12:00:00Z");
    private static final Periodo NOVO_PERIODO = new Periodo(NOVO_INICIO, NOVO_FIM);

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    @Mock
    private VerificadorDeConflitoDeAgenda verificadorDeConflito;

    private ReagendarRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReagendarRondaUseCase(agendamentoRepository, verificadorDeConflito,
                Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void shouldRescheduleAgendamento() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRondaResponse response = useCase.executar("agendamento-1",
                command(NOVO_INICIO, NOVO_FIM));

        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoResponse(NOVO_INICIO, NOVO_FIM));
        verify(verificadorDeConflito).verificar("Maria", NOVO_PERIODO, "agendamento-1");
        verify(agendamentoRepository).salvar(agendamento);
    }

    @Test
    void shouldThrowWhenAgendamentoNotFound() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente", command(NOVO_INICIO, NOVO_FIM)))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Agendamento")
                .hasMessageContaining("inexistente");

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotRescheduleWhenStatusIsNotAgendada() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.EM_ANDAMENTO);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar("agendamento-1", command(NOVO_INICIO, NOVO_FIM)))
                .isExactlyInstanceOf(ReagendamentoNaoPermitidoException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldPropagateConflictIgnoringItself() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));
        doThrow(new ConflitoDeAgendaException("Maria", NOVO_PERIODO))
                .when(verificadorDeConflito).verificar("Maria", NOVO_PERIODO, "agendamento-1");

        assertThatThrownBy(() -> useCase.executar("agendamento-1", command(NOVO_INICIO, NOVO_FIM)))
                .isExactlyInstanceOf(ConflitoDeAgendaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotRescheduleToThePast() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));
        Instant passado = AGORA.minusSeconds(3600);

        assertThatThrownBy(() -> useCase.executar("agendamento-1", command(passado, FIM)))
                .isExactlyInstanceOf(InicioNoPassadoException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    private static ReagendarRondaCommand command(Instant inicio, Instant fim) {
        return new ReagendarRondaCommand(new PeriodoCommand(inicio, fim));
    }
}

package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.AgendarRondaCommand;
import br.com.schedule.application.command.PeriodoCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.application.response.PeriodoResponse;
import br.com.schedule.domain.exception.ConflitoDeAgendaException;
import br.com.schedule.domain.exception.InicioNoPassadoException;
import br.com.schedule.domain.exception.RondaInativaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import br.com.schedule.domain.repository.RondaRepository;
import br.com.schedule.domain.service.VerificadorDeConflitoDeAgenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.AGORA;
import static br.com.schedule.application.ApplicationTestFixtures.FIM;
import static br.com.schedule.application.ApplicationTestFixtures.INICIO;
import static br.com.schedule.application.ApplicationTestFixtures.PERIODO;
import static br.com.schedule.application.ApplicationTestFixtures.ronda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendarRondaUseCaseTest {

    @Mock
    private RondaRepository rondaRepository;

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    @Mock
    private VerificadorDeConflitoDeAgenda verificadorDeConflito;

    private AgendarRondaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AgendarRondaUseCase(rondaRepository, agendamentoRepository,
                verificadorDeConflito, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void shouldScheduleRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRondaResponse response = useCase.executar(command(" Maria "));

        assertThat(response.id()).isNotBlank();
        assertThat(response.idRonda()).isEqualTo("ronda-1");
        assertThat(response.responsavel()).isEqualTo("Maria");
        assertThat(response.periodoPrevisto()).isEqualTo(new PeriodoResponse(INICIO, FIM));
        assertThat(response.status()).isEqualTo(StatusAgendamento.AGENDADA);
        assertThat(response.motivoCancelamento()).isNull();

        verify(verificadorDeConflito).verificar("Maria", PERIODO);

        ArgumentCaptor<AgendamentoRonda> captor = ArgumentCaptor.forClass(AgendamentoRonda.class);
        verify(agendamentoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusAgendamento.AGENDADA);
        assertThat(captor.getValue().getId()).isEqualTo(response.id());
    }

    @Test
    void shouldThrowWhenRondaNotFound() {
        when(rondaRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new AgendarRondaCommand(
                "inexistente", "Maria", new PeriodoCommand(INICIO, FIM))))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Ronda")
                .hasMessageContaining("inexistente");

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldPropagateConflict() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        doThrow(new ConflitoDeAgendaException("Maria", PERIODO))
                .when(verificadorDeConflito).verificar("Maria", PERIODO);

        assertThatThrownBy(() -> useCase.executar(command("Maria")))
                .isExactlyInstanceOf(ConflitoDeAgendaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotScheduleInactiveRonda() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1", false)));

        assertThatThrownBy(() -> useCase.executar(command("Maria")))
                .isExactlyInstanceOf(RondaInativaException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotScheduleInThePast() {
        when(rondaRepository.buscarPorId("ronda-1")).thenReturn(Optional.of(ronda("ronda-1")));
        PeriodoCommand passado = new PeriodoCommand(AGORA.minusSeconds(3600), FIM);

        assertThatThrownBy(() -> useCase.executar(
                new AgendarRondaCommand("ronda-1", "Maria", passado)))
                .isExactlyInstanceOf(InicioNoPassadoException.class);

        verifyNoInteractions(verificadorDeConflito);
        verify(agendamentoRepository, never()).salvar(any());
    }

    private static AgendarRondaCommand command(String responsavel) {
        return new AgendarRondaCommand("ronda-1", responsavel, new PeriodoCommand(INICIO, FIM));
    }
}

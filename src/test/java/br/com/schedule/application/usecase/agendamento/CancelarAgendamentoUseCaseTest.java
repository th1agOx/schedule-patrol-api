package br.com.schedule.application.usecase.agendamento;

import br.com.schedule.application.command.CancelarAgendamentoCommand;
import br.com.schedule.application.exception.RecursoNaoEncontradoException;
import br.com.schedule.application.response.AgendamentoRondaResponse;
import br.com.schedule.domain.exception.MotivoCancelamentoObrigatorioException;
import br.com.schedule.domain.exception.TransicaoDeStatusInvalidaException;
import br.com.schedule.domain.model.AgendamentoRonda;
import br.com.schedule.domain.model.StatusAgendamento;
import br.com.schedule.domain.repository.AgendamentoRondaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.com.schedule.application.ApplicationTestFixtures.agendamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAgendamentoUseCaseTest {

    @Mock
    private AgendamentoRondaRepository agendamentoRepository;

    private CancelarAgendamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelarAgendamentoUseCase(agendamentoRepository);
    }

    @Test
    void shouldCancelAgendamento() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.salvar(any(AgendamentoRonda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgendamentoRondaResponse response = useCase.executar("agendamento-1",
                new CancelarAgendamentoCommand("  Sem equipe  "));

        assertThat(response.status()).isEqualTo(StatusAgendamento.CANCELADA);
        assertThat(response.motivoCancelamento()).isEqualTo("Sem equipe");
        verify(agendamentoRepository).salvar(agendamento);
    }

    @Test
    void shouldThrowWhenAgendamentoNotFound() {
        when(agendamentoRepository.buscarPorId("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente",
                new CancelarAgendamentoCommand("Sem equipe")))
                .isExactlyInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Agendamento")
                .hasMessageContaining("inexistente");

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldRequireCancellationReason() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.AGENDADA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar("agendamento-1",
                new CancelarAgendamentoCommand("   ")))
                .isExactlyInstanceOf(MotivoCancelamentoObrigatorioException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }

    @Test
    void shouldNotCancelFinishedAgendamento() {
        AgendamentoRonda agendamento = agendamento("agendamento-1", StatusAgendamento.CONCLUIDA);
        when(agendamentoRepository.buscarPorId("agendamento-1")).thenReturn(Optional.of(agendamento));

        assertThatThrownBy(() -> useCase.executar("agendamento-1",
                new CancelarAgendamentoCommand("Sem equipe")))
                .isExactlyInstanceOf(TransicaoDeStatusInvalidaException.class);

        verify(agendamentoRepository, never()).salvar(any());
    }
}
